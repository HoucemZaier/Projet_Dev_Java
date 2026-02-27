package Services;

import java.io.InputStream;
import java.util.Properties;

public class EmailConfigService {

    private static Properties properties = new Properties();

    static {
        try {
            InputStream input = EmailConfigService.class
                    .getClassLoader()
                    .getResourceAsStream("email.properties");

            properties.load(input);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUsername() {
        return properties.getProperty("mail.username");
    }

    public static String getPassword() {
        return properties.getProperty("mail.password");
    }

    public static String getHost() {
        return properties.getProperty("mail.smtp.host");
    }

    public static String getPort() {
        return properties.getProperty("mail.smtp.port");
    }
}