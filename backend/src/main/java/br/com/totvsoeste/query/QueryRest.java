package br.com.totvsoeste.query;

import br.com.totvsoeste.query.model.QueryDados;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/query")
public class QueryRest {

    private static final Logger log = LoggerFactory.getLogger(QueryRest.class);

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response ping() {
        try {

            JSONObject jp = new JSONObject();
            jp.put("status", "pong");
            jp.put("version", 9);

            return Response.status(200).entity(jp.toString()).build();

        } catch (Exception e) {
            log.error("ERRO REST: " + e.getMessage(), e);
            JSONObject jp = new JSONObject();
            jp.put("erro", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jp.toString()).build();
        }
    }

    @POST
    @Path("/test-builder-sql")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testBuilderSql(QueryDados queryDados) {
        LogIntegracao logi = new LogIntegracao(queryDados.isDebug());

        try {

            QueryBuilder builder = new QueryBuilder(queryDados, logi);

            JSONObject jp = new JSONObject();

            jp.put("sql", builder.getQuery());

            if (queryDados.isDebug()) {
                logi.inserirLog("SUCESSO");
            }

            return Response.status(200).entity(jp.toString()).build();

        } catch (Exception e) {
            log.error("ERRO REST: " + e.getMessage(), e);
            JSONObject jp = new JSONObject();
            jp.put("erro", e.getMessage());
            logi.inserirLog("ERRO");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jp.toString()).build();
        }
    }

    @POST
    @Path("/consultar/{tipoConsulta}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response consultar(QueryDados queryDados, @PathParam("tipoConsulta") String tipoConsulta) {

        LogIntegracao logi = new LogIntegracao(queryDados.isDebug());

        try {

            if ((!tipoConsulta.equals("lista")) && (!tipoConsulta.equals("dataset"))) {
                throw new Exception("Não existe o tipo de consulta '" + tipoConsulta + "', os tipos possiveis são: 'lista', 'dataset'.");
            }

            Query query = new Query(queryDados, logi);

            JSONObject jp = query.consultar(tipoConsulta);

            if (queryDados.isDebug()) {
                logi.inserirLog("SUCESSO");
            }

            return Response.status(200).entity(jp.toString()).build();

        } catch (Exception e) {
            log.error("ERRO REST: " + e.getMessage(), e);
            JSONObject jp = new JSONObject();
            jp.put("erro", e.getMessage());
            logi.inserirLog("ERRO");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jp.toString()).build();
        }
    }

    @GET
    @Path("/datasources")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response datasources() {
        LogIntegracao logi = new LogIntegracao(false);
        try {

            QueryDados dados = new QueryDados();
            dados.setDebug(false);
            dados.setDatasource("AppDS");
            dados.setConsulta("SELECT NOM_SERV_DADOS FROM SERV_DADOS WHERE IDI_TIP_SERV = 4 AND COD_EMPRESA = '1'");

            Query query = new Query(dados, logi);

            JSONObject jp = query.consultar("lista");

            if (dados.isDebug()) {
                logi.inserirLog("SUCESSO");
            }

            return Response.status(200).entity(jp.toString()).build();

        } catch (Exception e) {
            log.error("ERRO REST: " + e.getMessage(), e);
            JSONObject jp = new JSONObject();
            jp.put("erro", e.getMessage());
            logi.inserirLog("ERRO");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jp.toString()).build();
        }
    }

    @POST
    @Path("/executar")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executar(QueryDados queryDados) {

        LogIntegracao logi = new LogIntegracao(queryDados.isDebug());

        try {

            Query query = new Query(queryDados, logi);

            boolean status = query.executar();

            if (queryDados.isLogi()) {
                String statusLog = (status) ? "SUCESSO" : "ERRO";
                logi.inserirLog(statusLog);
            }

            return Response.status(200).entity(query.retorno.toString()).build();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            JSONObject jp = new JSONObject();
            jp.put("status", false);
            jp.put("erro", e.getMessage());
            logi.inserirLog("ERRO");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jp.toString()).build();
        }
    }

}
