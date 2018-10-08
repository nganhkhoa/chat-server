import java.lang.*;
import java.io.*;
import java.util.*;

class Request{
    private String task;
    // [0]: name, [1]: password, [2]: IP, [3]: port
    private List<String> param;

    public String getTask(){
        return this.task;
    }

    public List<String> getParam(){
        return this.param;
    }

    
}