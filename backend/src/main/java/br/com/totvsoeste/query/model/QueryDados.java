package br.com.totvsoeste.query.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

@XmlRootElement
public class QueryDados implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(QueryDados.class);

    private boolean debug;
    private boolean logi;
    private boolean autoCommit;
    private String datasource;
    private String consulta;
    private String function;
    private Map<String, Object> dados;

    public QueryDados() {
        super();
        debug = false;
    }

    public void validarCampos() throws Exception {

    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getConsulta() {
        return consulta;
    }

    public void setConsulta(String consulta) {
        this.consulta = consulta;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Map<String, Object> getDados() {
        return dados;
    }

    public void setDados(Map<String, Object> dados) {
        this.dados = dados;
    }

    public boolean isLogi() {
        return logi;
    }

    public void setLogi(boolean logi) {
        this.logi = logi;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
}
