import java.lang.*;
import java.io.*;
import java.util.*;

public class Response {
    private int status;
    private String msg;

    // for responses with string returns
    private List<String> result;
    // for responses with boolean returns
    private boolean success;

    public Response(int status, String msg) {
        if (status % 100 == 0)
            this.success = true;
        else
            this.success = false;

        this.status = status;
        this.msg = msg;
    }

    public Response(int status, String msg, List<String> result) {
        if (status % 100 == 0)
            this.success = true;
        else
            this.success = false;

        this.status = status;
        this.msg = msg;
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public List<String> getResult() {
        return result;
    }

    public boolean success() {
        return success;
    }
}
