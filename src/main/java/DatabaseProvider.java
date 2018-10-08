public abstract class DatabaseProvider {
    public abstract boolean connect() throws Exception;
    public abstract boolean close();
    protected abstract Object getAccountDatabase();
    protected abstract Object getRoomDatabase();

    public abstract boolean signIn(String name, String password);
    public abstract void addUser(Account account);

    public abstract boolean existUser(String name);

    public abstract String getIP(String name);
    public abstract void setOnline(String name, String IP, String port);
    public abstract void setOffline(String name);
}
