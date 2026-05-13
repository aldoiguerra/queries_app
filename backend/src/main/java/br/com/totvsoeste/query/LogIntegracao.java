package br.com.totvsoeste.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogIntegracao {

    private static final Logger log = LoggerFactory.getLogger(LogIntegracao.class);
    public String mensagem;
    public boolean debug;

    public LogIntegracao(boolean debug) {

        this.debug = debug;
        mensagem = "";

    }

    public void logInfoC(String msg) {
        mensagem += "-------------------------------------------------- " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date()) + " :\n" + msg + "\n";
        if (debug) {
            log.info(msg);
        }
    }

    public void logInfoS(String msg) {
        mensagem += msg + "\n";
        if (debug) {
            log.info(msg);
        }
    }

    public void logErros(String msg, Exception ex) {
        log.error(msg, ex);
        mensagem += "--------------------------------------------------" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date()) + " - ERRO: " + "\n";
        mensagem += msg + "\n";
        mensagem += "--------------------------------------------------" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date()) + " - PILHA: " + "\n";
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        mensagem += exceptionAsString + "\n";
        mensagem += "--------------------------------------------------" + "\n";
    }

    public void inserirLog(String status) {

        Conexao conApp = null;
        try {
            String data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

            mensagem = mensagem.replaceAll("'", "");

            String queryInsert = "INSERT INTO TOE_LOG_INTEGRACAO " +
                    "(PROCESSO, DOCUMENTO, VERSAO, LOCAL, STATUS, MENSAGEM, EXECUCAO_INICIO, EXECUCAO_FIM) " +
                    "VALUES " +
                    "('', '', '', 'queries_app', '" + status + "', '" + mensagem + "', '" + data + "', '" + data + "') ";

            conApp = new Conexao("AppDS");
            Connection conn = conApp.getConnection();
            PreparedStatement stmt = conn.prepareStatement(queryInsert);
            stmt.execute();

            stmt.close();

        } catch (Exception ex) {
            log.error("ERRRO: ", ex);
        } finally {
            if (conApp != null) {
                conApp.close();
            }
        }

    }

}
