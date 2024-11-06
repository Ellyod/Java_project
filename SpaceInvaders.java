import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    //board
    int tileSize = 40;
    int rows = 20;
    int columns = 20;

    int boardWidth = tileSize * columns; // 32 * 16
    int boardHeight = tileSize * rows; // 32 * 16

    Image shipImg;
    Image khornImg;
    Image tzeetchImg;
    Image nurgleImg;
    Image slaneeshImg;
    ArrayList<Image> EnemyImageArray;

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive = true; //used for enemy
        boolean used = false; //used for bullets
        
        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    //ship
    int shipWidth = tileSize*2;
    int shipHeight = tileSize*2;
    int shipX = tileSize * columns/2 - tileSize;
    int shipY = tileSize * rows - tileSize*2;
    int shipVelocityX = tileSize; //ship moving speed
    Block ship;

    //enemy
    ArrayList<Block> EnemyArray;
    int EnemyWidth = tileSize*2;
    int EnemyHeight = tileSize*2;
    int EnemyX = tileSize;
    int EnemyY = tileSize;

    int EnemyRows = 2;
    int EnemyColumns = 3;
    int EnemyCount = 0; //number of enemy to defeat
    int EnemyVelocityX = 1; //enemy moving speed

    //bullets
    ArrayList<Block> bulletArray;
    int bulletWidth = tileSize/8;
    int bulletHeight = tileSize/2;
    int bulletVelocityY = -15; //bullet moving speed

    Timer gameLoop;
    boolean gameOver = false;
    int score = 0;

    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        //load images
        shipImg = new ImageIcon(getClass().getResource("./ship.png")).getImage();
        khornImg = new ImageIcon(getClass().getResource("./khorn.png")).getImage();
        tzeetchImg = new ImageIcon(getClass().getResource("./Tzeetch.png")).getImage();
        nurgleImg = new ImageIcon(getClass().getResource("./nurgle.png")).getImage();
        slaneeshImg = new ImageIcon(getClass().getResource("./Slaneesh.png")).getImage();

        EnemyImageArray = new ArrayList<Image>();
        EnemyImageArray.add(khornImg);
        EnemyImageArray.add(tzeetchImg);
        EnemyImageArray.add(nurgleImg);
        EnemyImageArray.add(slaneeshImg);

        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImg);
        EnemyArray = new ArrayList<Block>();
        bulletArray = new ArrayList<Block>();

        //game timer
        gameLoop = new Timer(1000/60, this); //1000/60 = 16.6
        createEnemy();
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //ship
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        //enemy
        for (int i = 0; i < EnemyArray.size(); i++) {
            Block Enemy = EnemyArray.get(i);
            if (Enemy.alive) {
                g.drawImage(Enemy.img, Enemy.x, Enemy.y, Enemy.width, Enemy.height, null);
            }
        }

        //bullets
        g.setColor(Color.white);
        for (int i = 0; i < bulletArray.size(); i++) {
            Block bullet = bulletArray.get(i);
            if (!bullet.used) {
                g.drawRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        //score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString("Score: " + String.valueOf((int) score ), 10, 35);
        }
    }

    public void move() {
        //Enemy
        for (int i = 0; i < EnemyArray.size(); i++) {
            Block Enemy = EnemyArray.get(i);
            if (Enemy.alive) {
                Enemy.x += EnemyVelocityX;

                //if Enemy touches the borders
                if (Enemy.x + Enemy.width >= boardWidth || Enemy.x <= 0) {
                    EnemyVelocityX *= -1;
                    Enemy.x += EnemyVelocityX*2;

                    //move all Enemy up by one row
                    for (int j = 0; j < EnemyArray.size(); j++) {
                        EnemyArray.get(j).y += EnemyHeight;
                    }
                }

                if (Enemy.y >= ship.y) {
                    gameOver = true;
                }
            }
        }

        //bullets
        for (int i = 0; i < bulletArray.size(); i++) {
            Block bullet = bulletArray.get(i);
            bullet.y += bulletVelocityY;

            //bullet collision with enemy
            for (int j = 0; j < EnemyArray.size(); j++) {
                Block Enemy = EnemyArray.get(j);
                if (!bullet.used && Enemy.alive && detectCollision(bullet, Enemy)) {
                    bullet.used = true;
                    Enemy.alive = false;
                    EnemyCount--;
                    score += 100;
                }
            }
        }

        //clear bullets
        while (bulletArray.size() > 0 && (bulletArray.get(0).used || bulletArray.get(0).y < 0)) {
            bulletArray.remove(0); //removes the first element of the array
        }

        //next level
        if (EnemyCount == 0) {
            //increase the number of enemys in columns and rows by 1
            score += EnemyColumns * EnemyRows * 100; 
            EnemyColumns = Math.min(EnemyColumns + 1, columns/2 -2); //cap at 20/2 -2 = 8
            EnemyRows = Math.min(EnemyRows + 1, rows-8);  //cap at 20-8 = 12
            EnemyArray.clear();
            bulletArray.clear();
            createEnemy();
        }
    }

    public void createEnemy() {
        Random random = new Random();
        for (int i = 0; i < EnemyColumns; i++) {
            for (int j = 0; j < EnemyRows; j++) {
                int randomImgIndex = random.nextInt(EnemyImageArray.size());
                Block Enemy = new Block(
                    EnemyX + i*EnemyWidth, 
                    EnemyY + j*EnemyHeight, 
                    EnemyWidth, 
                    EnemyHeight,
                    EnemyImageArray.get(randomImgIndex)
                );
                EnemyArray.add(Enemy);
            }
        }
        EnemyCount = EnemyArray.size();
    }
    

    public boolean detectCollision(Block a, Block b) {
        return  a.x < b.x + b.width &&  //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&  //a's top right corner passes b's top left corner
                a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;   //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) { //any key to restart
            ship.x = shipX;
            bulletArray.clear();
            EnemyArray.clear();
            gameOver = false;
            score = 0;
            EnemyColumns = 3;
            EnemyRows = 2;
            EnemyVelocityX = 1;
            createEnemy();
            gameLoop.start();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT  && ship.x - shipVelocityX >= 0) {
            ship.x -= shipVelocityX; //move left
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT  && ship.x + shipVelocityX + ship.width <= boardWidth) {
            ship.x += shipVelocityX; //move right
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            //shoot bullet
            Block bullet = new Block(ship.x + shipWidth*15/32, ship.y, bulletWidth, bulletHeight, null);
            bulletArray.add(bullet);
        }
    }
}