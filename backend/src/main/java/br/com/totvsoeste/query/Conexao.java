package br.com.totvsoeste.query;

import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.json.JSONObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Conexao {

  private DataSource dataSource;
  private Connection conn;

  public Conexao(String datasource) throws NamingException, SQLException {
    InitialContext ic = new javax.naming.InitialContext();
    dataSource = (DataSource) ic.lookup("java:/jdbc/" + datasource);
    this.conn = dataSource.getConnection();
  }

  public Connection getConnection() throws SQLException {
    if (conn == null) {
      this.conn = dataSource.getConnection();
    }
    return this.conn;
  }

  public void close() {
    try {
      if (conn != null) {
        this.conn.close();
        this.conn = null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List getListaJson(ResultSet rs) throws SQLException {

    List listaDados = new ArrayList<>();

    ResultSetMetaData md = rs.getMetaData();
    int numCols = md.getColumnCount();
    List<String> colNames = IntStream.range(0, numCols)
        .mapToObj(i -> {
          try {
            return md.getColumnName(i + 1);
          } catch (SQLException e) {
            e.printStackTrace();
            return "?";
          }
        })
        .collect(Collectors.toList());

    listaDados = DSL.using(conn)
        .fetch(rs)
        .map((RecordMapper) r -> {
          JSONObject obj = new JSONObject();
          colNames.forEach(cn -> obj.put(cn, r.get(cn)));
          return obj;
        });

    return listaDados;
  }

  public List executeQuery(PreparedStatement stmt) throws Exception {

    List listaDados = new ArrayList<>();
    try {
      ResultSet rs = stmt.executeQuery();

      ResultSetMetaData md = rs.getMetaData();
      int numCols = md.getColumnCount();
      List<String> colNames = IntStream.range(0, numCols)
          .mapToObj(i -> {
            try {
              return md.getColumnName(i + 1);
            } catch (SQLException e) {
              e.printStackTrace();
              return "?";
            }
          })
          .collect(Collectors.toList());

      listaDados = DSL.using(conn)
          .fetch(rs)
          .map((RecordMapper) r -> {
            JSONObject obj = new JSONObject();
            colNames.forEach(cn -> obj.put(cn, r.get(cn)));
            return obj;
          });

    } catch (Exception e) {
      throw e;
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }

    return listaDados;
  }

  public List executeQuery(PreparedStatement stmt, RecordMapper recordMapper) throws Exception {

    List listaDados = new ArrayList<>();
    try {
      ResultSet rs = stmt.executeQuery();

      ResultSetMetaData md = rs.getMetaData();
      int numCols = md.getColumnCount();
      List<String> colNames = IntStream.range(0, numCols)
          .mapToObj(i -> {
            try {
              return md.getColumnName(i + 1);
            } catch (SQLException e) {
              e.printStackTrace();
              return "?";
            }
          })
          .collect(Collectors.toList());

      listaDados = DSL.using(conn)
          .fetch(rs)
          .map(recordMapper);

    } catch (Exception e) {
      throw e;
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }

    return listaDados;
  }


}
