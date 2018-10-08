import java.lang.*;
import java.io.*;
import java.util.*;

public class Request {
    private String task;
    private List<String> param;

    public Request(String task) {
        this.task = task;
    }

    public Request(String task, List<String> param) {
        this.task = task;
        this.param = param;
    }

    public String getTask() {
        return this.task;
    }

    public List<String> getParam() {
        return this.param;
    }
}
