package br.com.totvsoeste.query;

import br.com.totvsoeste.query.model.QueryDados;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Query {

    private static final Logger log = LoggerFactory.getLogger(Query.class);
    private QueryDados dados;
    private LogIntegracao logi;
    private JSONObject infos;
    public JSONObject retorno;

    public Query(QueryDados dados, LogIntegracao logi) {

        this.dados = dados;
        this.logi = logi;
        this.infos = new JSONObject();
        this.retorno = new JSONObject();

    }

    public JSONObject consultar(String tipoConsulta) throws Exception {

        JSONObject retorno = new JSONObject();

        logi.logInfoC("Iniciando consultar()...");
        logi.logInfoS("consultar() => dados.getDataset(): " + dados.getDatasource());
        logi.logInfoS("consultar() => dados.getConsulta(): " + dados.getConsulta());
        long inicio = 0;
        long inicioT = new Date().getTime();

        try {

            InitialContext ic = new InitialContext();
            DataSource dataSource = (DataSource) ic.lookup("java:/jdbc/" + dados.getDatasource());

            try (Connection conn = dataSource.getConnection()) {

                String consulta = "";
                if ((dados.getConsulta() != null) && (!dados.getConsulta().equals(""))) {
                    consulta = dados.getConsulta();
                } else {
                    QueryBuilder builder = new QueryBuilder(dados, logi);
                    consulta = builder.getQuery();
                }

                if (consulta.trim().equals("")) {
                    throw new Exception("Não foi informada nenhuma query para executar, passe o parametro 'consulta' ou 'function' no json.");
                }

                inicio = new Date().getTime();

                try (PreparedStatement stmt = conn.prepareStatement(consulta)) {

                    try (ResultSet rs = stmt.executeQuery()) {

                        double tempo_execute = ((new Date().getTime() - inicio) / 1000.0);
                        infos.put("t1.tempo_execute", tempo_execute);
                        logi.logInfoS("consultar() => tempo execute: " + tempo_execute + " seg");
                        inicio = new Date().getTime();

                        if ("dataset".equals(tipoConsulta)) {

                            ResultSetMetaData md = rs.getMetaData();
                            int numCols = md.getColumnCount();

                            List<String> colNames = IntStream.range(0, numCols).mapToObj(i -> {
                                try {
                                    return md.getColumnLabel(i + 1);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    return "?";
                                }
                            }).collect(Collectors.toList());

                            double tempo_columns = ((new Date().getTime() - inicio) / 1000.0);
                            infos.put("t2.tempo_columns", tempo_columns);
                            logi.logInfoS("consultar() => tempo columns: " + tempo_columns + " seg");
                            inicio = new Date().getTime();

                            List<JSONObject> listaDados = DSL.using(conn).fetch(rs).map((RecordMapper) r -> {
                                JSONObject obj = new JSONObject();
                                colNames.forEach(cn -> obj.put(cn, r.get(cn)));
                                return obj;
                            });

                            double tempo_values = ((new Date().getTime() - inicio) / 1000.0);
                            infos.put("t3.tempo_values", tempo_values);
                            logi.logInfoS("consultar() => tempo values: " + tempo_values + " seg");

                            logi.logInfoS("consultar() => colNames.size(): " + colNames.size());
                            logi.logInfoS("consultar() => listaDados.size(): " + listaDados.size());

                            retorno.put("columns", new JSONArray(colNames));
                            retorno.put("values", new JSONArray(listaDados));

                        } else if ("lista".equals(tipoConsulta)) {

                            String jsonQuery = DSL.using(conn).fetch(rs).formatJSON();

                            double tempo_formatJSON = ((new Date().getTime() - inicio) / 1000.0);
                            infos.put("t2.tempo_formatJSON", tempo_formatJSON);
                            logi.logInfoS("consultar() => tempo formatJSON: " + tempo_formatJSON + " seg");

                            retorno = new JSONObject(jsonQuery);

                            logi.logInfoS("consultar() => fields.length(): " + retorno.getJSONArray("fields").length());
                            logi.logInfoS("consultar() => records.length(): " + retorno.getJSONArray("records").length());

                        }
                    }

                    double tempo_total = ((new Date().getTime() - inicioT) / 1000.0);
                    infos.put("t4.tempo_total", tempo_total);
                    logi.logInfoS("consultar() => tempo total: " + tempo_total + " seg");

                    retorno.put("infos", infos);

                }

            }

        } catch (Exception e) {
            logi.logErros("ERRO ao efetuar consulta: " + e.getMessage(), e);
            throw e;
        }

        return retorno;
    }

    public boolean executar() {

        boolean status = false;
        retorno.put("status", status);

        logi.logInfoC("Iniciando executar()...");
        logi.logInfoS("executar() => dados.getDataset(): " + dados.getDatasource());
        logi.logInfoS("executar() => dados.getConsulta(): " + dados.getConsulta());
        long inicio = 0;

        long inicioG = new Date().getTime();

        try {

            InitialContext ic = new InitialContext();
            DataSource dataSource = (DataSource) ic.lookup("java:/jdbc/" + dados.getDatasource());

            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(dados.isAutoCommit());
                try {
                    JSONArray results = new JSONArray();
                    String myQuery = dados.getConsulta();

                    for (String sql : splitSqlStatements(myQuery)) {

                        logi.logInfoC("sql => " + sql);

                        if (sql.isBlank()) continue;

                        JSONObject execucao = new JSONObject();
                        try (Statement stmt = conn.createStatement()) {

                            inicio = new Date().getTime();

                            boolean isResultSet = stmt.execute(sql);

                            double tempo_execucao = ((new Date().getTime() - inicio) / 1000.0);
                            execucao.put("tempo_execucao", tempo_execucao);
                            logi.logInfoS("executar() => tempo_execucao: " + tempo_execucao);

                            logi.logInfoC("");

                            execucao.put("isResultSet", isResultSet);
                            logi.logInfoS("executar() => isResultSet: " + isResultSet);

                            int rowCount = 0;
                            inicio = new Date().getTime();
                            if (isResultSet) {
                                try (ResultSet rs = stmt.getResultSet()) {

                                    String jsonQuery = DSL.using(conn).fetch(rs).formatJSON();

                                    double tempo_fetch = ((new Date().getTime() - inicio) / 1000.0);
                                    execucao.put("tempo_fetch", tempo_fetch);
                                    logi.logInfoS("executar() => tempo_fetch: " + tempo_fetch);
                                    inicio = new Date().getTime();

                                    JSONObject result = new JSONObject(jsonQuery);
                                    execucao.put("result", result);

                                    double tempo_json = ((new Date().getTime() - inicio) / 1000.0);
                                    execucao.put("tempo_json", tempo_json);
                                    logi.logInfoS("executar() => tempo_json: " + tempo_json);

                                    rowCount = result.getJSONArray("records").length();
                                    execucao.put("rows_count", rowCount);
                                    logi.logInfoS("executar() => rowCount: " + rowCount);

                                }
                            } else {

                                rowCount = stmt.getUpdateCount();

                                execucao.put("rows_count", rowCount);
                                logi.logInfoS("executar() => rowCount: " + rowCount);

                                double tempo_fetch = ((new Date().getTime() - inicio) / 1000.0);
                                execucao.put("tempo_fetch", tempo_fetch);
                                execucao.put("tempo_json", 0);
                                logi.logInfoS("executar() => tempo_fetch: " + tempo_fetch);

                            }

                            results.put(execucao);
                        }
                    }

                    retorno.put("results", results);

                    if (!dados.isAutoCommit()) conn.commit();
                } catch (Exception e) {
                    if (!dados.isAutoCommit()) conn.rollback();
                    throw e;
                }
            }

            status = true;
            retorno.put("status", status);

        } catch (Exception e) {

            status = false;
            retorno.put("status", status);
            retorno.put("erro", e.getMessage());

            logi.logErros(e.getMessage(), e);

        } finally {

            double tempo_total = ((new Date().getTime() - inicioG) / 1000.0);
            retorno.put("tempo_total", tempo_total);

        }

        return status;
    }

    /**
     * Quebra um script SQL em statements individuais, separados por ';'.
     * Respeita:
     * - strings com aspas simples (incluindo escape de '' dentro da string)
     * - identificadores entre aspas duplas
     * - comentários de linha (-- até o fim da linha)
     * - comentários em bloco (/* ... *&#47;)
     * <p>
     * NÃO trata blocos PL/SQL (BEGIN...END;) — para Oracle/procedures envie o bloco
     * inteiro como um único statement separado.
     */
    public static List<String> splitSqlStatements(String script) {
        List<String> statements = new ArrayList<>();
        if (script == null || script.isBlank()) {
            return statements;
        }

        StringBuilder current = new StringBuilder();
        int len = script.length();
        int i = 0;

        boolean inSingleQuote = false;   // dentro de 'texto'
        boolean inDoubleQuote = false;   // dentro de "identificador"
        boolean inLineComment = false;   // dentro de -- comentário
        boolean inBlockComment = false;  // dentro de /* comentário */

        while (i < len) {
            char c = script.charAt(i);
            char next = (i + 1 < len) ? script.charAt(i + 1) : '\0';

            // --- Encerramento de comentário de linha ---
            if (inLineComment) {
                current.append(c);
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                i++;
                continue;
            }

            // --- Encerramento de comentário em bloco ---
            if (inBlockComment) {
                current.append(c);
                if (c == '*' && next == '/') {
                    current.append(next);
                    inBlockComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }

            // --- Dentro de string com aspas simples ---
            if (inSingleQuote) {
                current.append(c);
                if (c == '\'') {
                    // '' é escape de aspa dentro da string — não fecha
                    if (next == '\'') {
                        current.append(next);
                        i += 2;
                        continue;
                    }
                    inSingleQuote = false;
                }
                i++;
                continue;
            }

            // --- Dentro de identificador com aspas duplas ---
            if (inDoubleQuote) {
                current.append(c);
                if (c == '"') {
                    if (next == '"') {              // "" é escape de aspa dupla
                        current.append(next);
                        i += 2;
                        continue;
                    }
                    inDoubleQuote = false;
                }
                i++;
                continue;
            }

            // --- Fora de qualquer contexto especial: detectar inícios ---
            if (c == '-' && next == '-') {
                inLineComment = true;
                current.append(c).append(next);
                i += 2;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                current.append(c).append(next);
                i += 2;
                continue;
            }
            if (c == '\'') {
                inSingleQuote = true;
                current.append(c);
                i++;
                continue;
            }
            if (c == '"') {
                inDoubleQuote = true;
                current.append(c);
                i++;
                continue;
            }

            // --- Separador de statement ---
            if (c == ';') {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current.setLength(0);
                i++;
                continue;
            }

            current.append(c);
            i++;
        }

        // Último statement (caso o script não termine com ';')
        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }

        return statements;
    }

//  public static void main(String[] args) {
//
//    SQLServerDataSource dataSource = new SQLServerDataSource();
//    dataSource.setUser("aldo.guerra"); // e.g., "sa"
//    dataSource.setPassword("6ugYwTwH-d-5!\"H");
//    dataSource.setServerName("10.2.15.20"); // e.g., "localhost"
//    dataSource.setDatabaseName("fluig_toeste_dev");
//    dataSource.setPortNumber(1433);
//
//
//    LogIntegracao logi = new LogIntegracao(false);
//
////    QueryDados dados = new QueryDados();
////    dados.setDebug(false);
////    dados.setDatasource("AppDS");
////    dados.setConsulta(
////        "SELECT FULL_NAME NOME,\n" +
////        "    EMAIL,\n" +
////        "    USER_CODE MATRICULA,\n" +
////        "    LOGIN,\n" +
////        "    (\n" +
////        "        SELECT COUNT(m8.empresa)\n" +
////        "        FROM DOCUMENTO d\n" +
////        "            JOIN ML00111388 m8 ON d.NR_DOCUMENTO = m8.documentid\n" +
////        "            AND d.NR_VERSAO = m8.version\n" +
////        "        WHERE VERSAO_ATIVA = 1\n" +
////        "            AND matricula = USER_CODE\n" +
////        "    ) EMPRESAFLUIG,\n" +
////        "    DATA_VALUE A3_COD\n" +
////        "FROM FDN_USERTENANT AS ut\n" +
////        "    INNER JOIN FDN_USER u ON u.USER_ID = ut.USER_ID\n" +
////        "    INNER JOIN FDN_USERDATA AS d ON ut.USER_TENANT_ID = d.USER_TENANT_ID\n" +
////        "    AND DATA_KEY = 'A3_COD';\n" +
////        "\n" +
////        "UPDATE FDN_USERTENANT SET PASSWORD = 'c983be8e8a7723c1059f10235e273253' WHERE LOGIN NOT IN ('wcmadmin', 'totvstech', 'nologin_1');\n" +
////        "\n" +
////        "SELECT TOP 100 * FROM FDN_USERTENANT;"
////        );
////
////    Query query = new Query(dados, logi);
////
////    query.executar();
//
//    QueryDados dados = new QueryDados();
//    dados.setDebug(false);
//    dados.setDatasource("AppDS");
//    dados.setConsulta("SELECT NOM_SERV_DADOS FROM SERV_DADOS WHERE IDI_TIP_SERV = 4 AND COD_EMPRESA = '1'");
//
//    Query query = new Query(dados, logi);
//
//    try {
//      JSONObject jp = query.consultar("lista");
//
//      System.out.println(jp.toString());
//      System.out.println("\n\n\n");
//      System.out.println(logi.mensagem);
//
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//
//  }

}
