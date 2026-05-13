package br.com.totvsoeste.query;

import br.com.totvsoeste.query.model.QueryDados;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueryBuilder {

    private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);
    private Conexao conexao = null;
    private QueryDados dados;
    private LogIntegracao logi;

    public QueryBuilder(QueryDados dados, LogIntegracao logi) {

        this.dados = dados;
        this.logi = logi;

    }

    public String getQuery() throws Exception {

        String funcao = getFuncao();

        String query = "";

        logi.logInfoC("getQuery() => executando javaScript");

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        JSONObject json = new JSONObject(dados.getDados());
        logi.logInfoS("getFuncao() => json.toString(): " + json.toString());

        engine.put("log", ScriptingLog.getInstance());

        engine.eval("var dados = " + json.toString() + ";");
        engine.eval(funcao);
        engine.eval("var query = " + dados.getFunction() + "(dados);");

        query = (String) engine.get("query");

        logi.logInfoS("getFuncao() => query: " + query);

        if (query.equals("")) {
            throw new Exception("Não foi retornada nenhuma query pela função: " + dados.getFunction());
        }

        return query;
    }

    private String getFuncao() throws Exception {

        String funcao = "";

        Conexao conApp = null;
        try {

            logi.logInfoC("getFuncao() => funcao com o nome: " + dados.getFunction());

            String query = "SELECT DSL_EVENT FROM event_geral WHERE COD_EVENT = ? ";

            conApp = new Conexao("AppDS");
            Connection conn = conApp.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, dados.getFunction());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                funcao = rs.getString("DSL_EVENT");
            }

            logi.logInfoS("getFuncao() => funcao: " + funcao);

            stmt.close();

        } catch (Exception ex) {
            log.error("ERRRO: ", ex);
            throw ex;
        } finally {
            if (conApp != null) {
                conApp.close();
            }
        }

        if (funcao.equals("")) {
            throw new Exception("Não foi encontado nenhum evento global definido como " + dados.getFunction());
        }

        return funcao;
    }

}
