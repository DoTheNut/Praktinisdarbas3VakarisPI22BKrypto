import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;

public class Main {

    public static BigInteger gcd(BigInteger a, BigInteger b) {
        while (!b.equals(BigInteger.ZERO)) {
            BigInteger temp = b;
            b = a.mod(b);
            a = temp;
        }
        return a;
    }

    public static BigInteger[] extendedGCD(BigInteger a, BigInteger b) {
        if (a.equals(BigInteger.ZERO)) {
            return new BigInteger[]{b, BigInteger.ZERO, BigInteger.ONE};
        }
        BigInteger[] vals = extendedGCD(b.mod(a), a);
        BigInteger g = vals[0];
        BigInteger y = vals[1];
        BigInteger x = vals[2];
        return new BigInteger[]{g, x.subtract(b.divide(a).multiply(y)), y};
    }

    public static BigInteger modInverse(BigInteger e, BigInteger phi) {
        BigInteger[] vals = extendedGCD(e, phi);
        return vals[1].mod(phi);
    }

    public static boolean isPrime(BigInteger n) {
        return n.isProbablePrime(1);
    }

    public static BigInteger[] generateKeys(BigInteger p, BigInteger q) throws Exception {
        if (!isPrime(p) || !isPrime(q)) {
            throw new Exception("Both numbers must be prime.");
        } else if (p.equals(q)) {
            throw new Exception("p and q cannot be the same");
        }

        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = BigInteger.valueOf(65537);  // Commonly used prime number for e
        if (!gcd(e, phi).equals(BigInteger.ONE)) {
            throw new Exception("e and phi(n) are not coprime.");
        }

        BigInteger d = modInverse(e, phi);

        return new BigInteger[]{n, e, d};  // n, e is public key; n, d is private key
    }

    public static BigInteger[] encrypt(String text, BigInteger[] publicKey) {
        BigInteger n = publicKey[0];
        BigInteger e = publicKey[1];
        BigInteger[] cipher = new BigInteger[text.length()];

        for (int i = 0; i < text.length(); i++) {
            cipher[i] = BigInteger.valueOf((int) text.charAt(i)).modPow(e, n);
        }

        return cipher;
    }

    public static String decrypt(BigInteger[] cipher, BigInteger[] privateKey) {
        BigInteger n = privateKey[0];
        BigInteger d = privateKey[1];
        StringBuilder text = new StringBuilder();

        for (BigInteger charCode : cipher) {
            text.append((char) charCode.modPow(d, n).intValue());
        }

        return text.toString();
    }

    public static void saveToFile(String filename, BigInteger[] data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (BigInteger datum : data) {
                writer.write(datum.toString());
                writer.newLine();
            }
        }
    }

    public static BigInteger[] loadFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return reader.lines().map(BigInteger::new).toArray(BigInteger[]::new);
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Įveskite pirmą pirminį skaičių p: ");
            BigInteger p = new BigInteger(scanner.nextLine());

            System.out.print("Įveskite antrą pirminį skaičių q: ");
            BigInteger q = new BigInteger(scanner.nextLine());

            System.out.print("Įveskite tekstą: ");
            String text = scanner.nextLine();

            // Raktų generavimas
            BigInteger[] keys = generateKeys(p, q);
            BigInteger[] publicKey = {keys[0], keys[1]};
            BigInteger[] privateKey = {keys[0], keys[2]};

            // Teksto šifravimas
            BigInteger[] cipher = encrypt(text, publicKey);

            // Šifruoto teksto ir viešojo rakto išsaugojimas
            saveToFile("encrypted.txt", cipher);
            saveToFile("public_key.txt", publicKey);

            // Šifruoto teksto nuskaitymas
            cipher = loadFromFile("encrypted.txt");

            // Teksto dešifravimas
            String decryptedText = decrypt(cipher, privateKey);

            System.out.println("Pradinis tekstas: " + text);
            System.out.println("Šifruotas tekstas: ");
            for (BigInteger c : cipher) {
                System.out.print(c + " ");
            }
            System.out.println("\nDešifruotas tekstas: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
