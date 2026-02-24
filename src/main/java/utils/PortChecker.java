package utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Port diagnostic utility to check if port 8080 is available
 */
public class PortChecker {

    public static void main(String[] args) {
        System.out.println("🔍 Port 8080 Diagnostic Tool");
        System.out.println("============================");

        checkPort(8080);

        // Also check if anything is using ngrok's default API port
        checkPort(4040);
    }

    public static boolean checkPort(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("✅ Port " + port + " is AVAILABLE");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Port " + port + " is OCCUPIED");
            System.err.println("💡 To find what's using this port, run:");
            System.err.println("   netstat -ano | findstr :" + port);
            System.err.println("   Then kill the process with: taskkill /PID <PID> /F");
            return false;
        }
    }

    public static void checkPortWithAdvice() {
        System.out.println("🔍 Checking port 8080 for OAuth server...");

        if (checkPort(8080)) {
            System.out.println("✅ PORT 8080 IS AVAILABLE");
            System.out.println("🚀 You can now start PlaNova and ngrok should work correctly");
            System.out.println("📋 Next steps:");
            System.out.println("   1. Start PlaNova application");
            System.out.println("   2. Make sure ngrok is running: ngrok http 8080 --scheme=https");
            System.out.println("   3. Test Google OAuth authentication");
        } else {
            System.out.println("❌ PORT 8080 IS OCCUPIED");
            System.out.println("🔧 CRITICAL FIX NEEDED:");
            System.out.println("   1. Find what's using port 8080:");
            System.out.println("      netstat -ano | findstr :8080");
            System.out.println("   2. Kill the process:");
            System.out.println("      taskkill /PID <ProcessID> /F");
            System.out.println("   3. Restart PlaNova application");
            System.out.println("");
            System.out.println("⚠️  Without port 8080, OAuth authentication will NOT work!");
        }
    }
}
