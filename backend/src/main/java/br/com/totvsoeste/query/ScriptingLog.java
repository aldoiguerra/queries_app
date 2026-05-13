package br.com.totvsoeste.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

public class ScriptingLog {
    private static Logger log;

    private static ScriptingLog instance;

    private ScriptingLog() {
        log = LoggerFactory.getLogger(ScriptingLog.class);
    }

    public static synchronized ScriptingLog getInstance() {
        if (instance == null)
            instance = new ScriptingLog();
        return instance;
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void trace(Object message, Throwable t) {
        log.trace(message.toString(), t);
    }

    public void trace(Object message) {
        log.trace(message.toString());
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void debug(Object message, Throwable t) {
        log.debug(message.toString(), t);
    }

    public void debug(Object message) {
        log.debug(message.toString());
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public void info(Object message, Throwable t) {
        log.info(message.toString(), t);
    }

    public void info(Object message) {
        log.info(message.toString());
    }

    public void log(Object message, Throwable t) {
        log.info(message.toString(), t);
    }

    public void log(Object message) {
        log.info(message.toString());
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public void warn(Object message, Throwable t) {
        log.warn(message.toString(), t);
    }

    public void warn(Object message) {
        log.warn(message.toString());
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public void error(Object message, Throwable t) {
        log.error(message.toString(), t);
    }

    public void error(Object message) {
        log.error(message.toString());
    }

    public boolean isFatalEnabled() {
        return log.isErrorEnabled();
    }

    public void fatal(Object message, Throwable t) {
        log.error(message.toString(), t);
    }

    public void fatal(Object message) {
        log.error(message.toString());
    }

    public void dir(Object obj) {
        try {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
//            StringWriter sw = new StringWriter();
//            mapper.writeValue(sw, obj);
//            log.info(sw.toString());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
