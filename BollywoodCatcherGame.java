import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class BollywoodCatcherGame extends JPanel implements ActionListener, KeyListener {
    private final Timer timer;
    private Movie fallingMovie;
    private final Basket[] baskets;
    private final String[] genres = {"Romance", "Comedy", "Action", "Family Drama"};
    private final int panelWidth = 800;
    private final int panelHeight = 600;
    private final Random random = new Random();
    private int score = 0;
    private int lives = 5;
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private boolean showWelcomeScreen = true;
    private int selectedBasket = 0;
    private boolean[] visibleBaskets = {true, true, true, true};

    public BollywoodCatcherGame() {
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        this.setBackground(Color.BLACK);

        baskets = new Basket[4];
        int basketWidth = 120;
        int basketHeight = 80;
        for (int i = 0; i < genres.length; i++) {
            baskets[i] = new Basket(
                (i * (panelWidth / 4)) + (panelWidth / 8) - (basketWidth / 2),
                panelHeight - basketHeight - 20,
                basketWidth, basketHeight, genres[i],
                new ImageIcon("images/" + genres[i].toLowerCase().replace(" ", "") + "_basket.png").getImage()
            );
        }

        timer = new Timer(30, this); // Slower falling speed
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showWelcomeScreen) {
            drawWelcomeScreen(g);
        } else if (gameOver) {
            drawGameOverScreen(g);
        } else if (gameRunning) {
            drawGameScreen(g);
        }
    }

    private void drawWelcomeScreen(Graphics g) {
        Image background = new ImageIcon("images/background.png").getImage();
        g.drawImage(background, 0, 0, panelWidth, panelHeight, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Welcome to Bollywood Catcher Game!", panelWidth / 2 - 220, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Instructions:", panelWidth / 2 - 80, 150);
        g.drawString("1. Catch the movies falling from the top.", panelWidth / 2 - 180, 180);
        g.drawString("2. Use keys 1, 2, 3, 4 to switch between baskets.", panelWidth / 2 - 180, 210);
        g.drawString("3. Match the movie's genre to the correct basket to score points.", panelWidth / 2 - 180, 240);
        g.drawString("4. You lose a life for missing or mismatching a movie.", panelWidth / 2 - 180, 270);
        g.drawString("5. Game ends when you lose all your lives.", panelWidth / 2 - 180, 300);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.GREEN);
        g.drawString("Press ENTER to Start", panelWidth / 2 - 100, 350);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, panelWidth, panelHeight);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Game Over!", panelWidth / 2 - 100, panelHeight / 2 - 50);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Your Score: " + score, panelWidth / 2 - 100, panelHeight / 2);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Press ENTER to Return to Welcome Screen", panelWidth / 2 - 160, panelHeight / 2 + 50);
    }

    private void drawGameScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + lives, 10, 40);
        g.drawString("Press 1: Romance | 2: Comedy | 3: Action | 4: Family Drama", 10, 70);

        g.setColor(Color.YELLOW);
        if (visibleBaskets[selectedBasket]) {
            g.drawRect(baskets[selectedBasket].x - 2, baskets[selectedBasket].y - 2,
                    baskets[selectedBasket].width + 4, baskets[selectedBasket].height + 4);
        }

        for (int i = 0; i < baskets.length; i++) {
            if (visibleBaskets[i]) {
                baskets[i].draw(g);
            }
        }

        if (fallingMovie != null) {
            fallingMovie.draw(g);
        }
    }

    private void resetGame() {
        score = 0;
        lives = 5;
        fallingMovie = null;
        gameRunning = false;
        gameOver = false;
        showWelcomeScreen = true;
        visibleBaskets = new boolean[]{true, true, true, true};
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameRunning && !gameOver) {
            if (fallingMovie == null) {
                String genre = genres[random.nextInt(genres.length)];
                String title = generateMovieTitle(genre);
                fallingMovie = new Movie(title, genre, random.nextInt(panelWidth - 100), 0, random.nextInt(3) + 2);
            } else {
                fallingMovie.y += fallingMovie.speed;

                if (fallingMovie.y > panelHeight) {
                    fallingMovie = null;
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                        repaint();
                        return;
                    }
                } else {
                    for (int i = 0; i < baskets.length; i++) {
                        if (baskets[i].catchMovie(fallingMovie)) {
                            if (fallingMovie.genre.equals(baskets[i].genre)) {
                                score++;
                            } else {
                                lives--;
                                if (lives <= 0) {
                                    gameOver = true;
                                    repaint();
                                    return;
                                }
                            }
                            fallingMovie = null;
                            break;
                        }
                    }
                }
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (showWelcomeScreen && keyCode == KeyEvent.VK_ENTER) {
            showWelcomeScreen = false;
            gameRunning = true;
            repaint();
        } else if (gameOver && keyCode == KeyEvent.VK_ENTER) {
            resetGame();
        } else if (gameRunning) {
            if (keyCode == KeyEvent.VK_LEFT) {
                baskets[selectedBasket].move(-20, panelWidth);
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                baskets[selectedBasket].move(20, panelWidth);
            } else if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_4) {
                int basketIndex = keyCode - KeyEvent.VK_1;
                selectedBasket = basketIndex;
                for (int i = 0; i < visibleBaskets.length; i++) {
                    visibleBaskets[i] = (i == basketIndex);
                }
            }
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private String generateMovieTitle(String genre) {
        switch (genre) {
            case "Romance":
                return new String[]{"Pathaan", "Dilwale", "Chennai Express", "Ae Dil Hai Mushkil"}[random.nextInt(4)];
            case "Comedy":
                return new String[]{"Housefull", "Golmaal Again", "Bala", "Dhamaal"}[random.nextInt(4)];
            case "Action":
                return new String[]{"Dhoom", "Ghajini", "Baahubali", "Singham"}[random.nextInt(4)];
            case "Family Drama":
                return new String[]{"Kabhi Khushi Kabhie Gham", "Baghban", "Kapoor & Sons", "The Sky Is Pink"}[random.nextInt(4)];
            default:
                return "Unknown";
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bollywood Catcher Game");
        BollywoodCatcherGame game = new BollywoodCatcherGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Basket class
    static class Basket {
        int x, y, width, height;
        String genre;
        Image image;

        Basket(int x, int y, int width, int height, String genre, Image image) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.genre = genre;
            this.image = image;
        }

        void draw(Graphics g) {
            g.drawImage(image, x, y, width, height, null);
        }

        void move(int dx, int panelWidth) {
            x = Math.max(0, Math.min(panelWidth - width, x + dx));
        }

        boolean catchMovie(Movie movie) {
            return new Rectangle(x, y, width, height).intersects(new Rectangle(movie.x, movie.y, 100, 20));
        }
    }

    // Movie class
    static class Movie {
        String title;
        String genre;
        int x, y;
        int speed;

        Movie(String title, String genre, int x, int y, int speed) {
            this.title = title;
            this.genre = genre;
            this.x = x;
            this.y = y;
            this.speed = speed;
        }

        void draw(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(title, x, y);
        }
    }
}
