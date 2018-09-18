public abstract class DatabaseProvider {
    public abstract boolean connect() throws Exception;
    public abstract boolean close();
    protected abstract Object getAccountDatabase();
    protected abstract Object getRoomDatabase();

    public abstract boolean signIn(String name, String password);
    public abstract String signUp(String name);
}
