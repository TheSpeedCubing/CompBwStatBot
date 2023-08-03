package idk;

import com.google.gson.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.awt.Font.BOLD;

public class main {

    static Font font;
    static BufferedImage bronze;
    static BufferedImage gold;
    static BufferedImage silver;
    static BufferedImage platinum;


    public static void main(String[] args) {
        final int PORT = 12345;
        ServerSocket serverSocket = null;
        try {
            font = new Font("DejaVu", 0, 40);
            // Load the font from the TTF file and derive it with bold style and size 100
//            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File("photo/Mosk_Extra-Light_200.ttf"));
//            Font boldFont = baseFont.deriveFont(Font.BOLD, 100f);
//            // Register the bold font with the graphics environment
//            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//            ge.registerFont(boldFont);
            font = font.deriveFont(BOLD, 100f);
            bronze = ImageIO.read(new File("photo/bronze.png"));
            gold = ImageIO.read(new File("photo/gold.png"));
            silver = ImageIO.read(new File("photo/silver.png"));
            platinum = ImageIO.read(new File("photo/platinum.png"));
            serverSocket = new ServerSocket(PORT);
        } catch (Exception e) {
            System.out.println("ex1");
            e.printStackTrace();
        }
        while (true) {
            try {
                System.out.println("listening");
                Socket clientSocket = serverSocket.accept();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                byte[] buffer = new byte[1024 * 8];
                in.read(buffer);
                String received = new String(buffer, StandardCharsets.UTF_8).replace("'", "\"");
                System.out.println("received");
                System.out.println(received);
                String[] s = received.split("\\|");
                System.out.println(Arrays.toString(s));
                a(Integer.parseInt(s[0]), Integer.parseInt(s[1]), s[2]);
                byte[] result = {1, 1};
                System.out.println("writing");
                out.write(result, 0, result.length);
                System.out.println("writed");
                // Close the socket after sending the response
            } catch (Exception e) {
                System.out.println("ex1");
                e.printStackTrace();
            }
        }
    }

    public static void drawInRect(Rectangle r, Graphics g, String str, Font f) {
        g.setFont(f);
        Font font = g.getFont();
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(str);
        while (textWidth > r.width) {
            font = font.deriveFont((float) (font.getSize() - 1));
            g.setFont(font);
            metrics = g.getFontMetrics(font);
            textWidth = metrics.stringWidth(str);
        }
        g.drawString(str, r.x + (r.width - textWidth) / 2, r.y + (r.height - metrics.getHeight()) / 2 + metrics.getAscent());
    }


    public static byte[] a(int players, int pos, String input) {
        try {
            JsonObject object = new JsonParser().parse(input.trim()).getAsJsonObject();
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);


//            String name = "Linm0n";
            String name = object.get("MC_id").getAsString();
//            String name = "russianashasemya";
            int elo = object.get("elo").getAsInt();
            int wins = object.get("wins").getAsInt();
            int mvps = object.get("mvps").getAsInt();
            int streak = object.get("ws").getAsInt();
            int games = object.get("games").getAsInt();
            int rate = (int) (mvps * 100D / games);
            double WLR = Math.floor(((double) wins) / (games - wins) * 100) / 100.0;

            //DB


            BufferedImage image;
            int progress;
            if (elo < 250) {
                image = bronze;
                progress = (int) ((elo / 250F) * 100);
            } else if (elo < 500) {
                image = silver;
                progress = (int) (((elo - 250) / 250F) * 100);
            } else if (elo < 1500) {
                image = gold;
                progress = (int) (((elo - 500) / 1000F) * 100);
            } else {
                image = platinum;
                progress = 100;
            }
            BufferedImage progressImg = ImageIO.read(new File("photo/progress bar/" + progress + ".png"));

            int w = Math.max(image.getWidth(), progressImg.getWidth());
            int h = Math.max(image.getHeight(), progressImg.getHeight());
            BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Graphics g = result.getGraphics();
            g.setFont(font);
            g.drawImage(image, 0, 0, null);
            g.drawImage(progressImg, 1048, 803, null);

            drawInRect(new Rectangle(240, 820, 300, 75), g, name, font.deriveFont(BOLD, 100f));
            drawInRect(new Rectangle(636, 218, 325, 150), g, Integer.toString(wins), font.deriveFont(Font.BOLD, 160f));
            drawInRect(new Rectangle(636, 218 + 460, 325, 150), g, Integer.toString(streak), font.deriveFont(Font.BOLD, 160f));
            drawInRect(new Rectangle(636 + 409, 218, 325, 150), g, "#" + pos, font.deriveFont(Font.BOLD, 160f));
            drawInRect(new Rectangle(636 + 409 + 409, 218, 325, 150), g, Integer.toString(mvps), font.deriveFont(Font.BOLD, 160f));
            g.setColor(Color.GRAY);
            drawInRect(new Rectangle(636, 218 + 200, 325, 70), g, WLR + " W/L", font.deriveFont(Font.PLAIN, 80f));
            drawInRect(new Rectangle(636 + 409, 218 + 200, 325, 70), g, "/" + players, font.deriveFont(Font.PLAIN, 80f));
            drawInRect(new Rectangle(636 + 409 + 409, 218 + 200, 325, 70), g, rate + "% RATE", font.deriveFont(Font.PLAIN, 80f));

            BufferedImage playerSkin = ImageIO.read(new URL("https://visage.surgeplay.com/full/832/" + name + ".png"));
            double pre = 0.8;
            g.drawImage(playerSkin, 145, 130, (int) (512 * pre), (int) (832 * pre), null);
            g.dispose();
            ImageIO.write(result, "PNG", new File("imgs/" + name + ".png"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(result, "png", baos);
//            byte[] bytes = baos.toByteArray();
//            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
