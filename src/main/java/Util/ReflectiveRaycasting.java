package Util;

import javax.swing.*;
import java.awt.*;
import Data.Line;
import Data.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class ReflectiveRaycasting extends JPanel implements MouseMotionListener, MouseListener {
    private final static int windowW = 700, windowH = 700;

    private final ArrayList<Line> paredes;
    private final ArrayList<Line> raycasts;
    private final ArrayList<Point> collisions;
    private final ArrayList<Line> reflections;
    private final ArrayList<Line> polygon;
    private final Point centro;
    
    private final int radius = 1000;
    private int numRaycasts = 50;
    private double lightAngle = 360.0;
    private double initialAngle = 0.0;
    
    private boolean togglePolygon = false;
    private boolean togglePolygonColor = false;
    private boolean reflect = false;
    private boolean drawingMode = false;
    
    private Point startPoint;
    private Point currentMousePosition;

    public ReflectiveRaycasting() {
        centro = new Point(windowW / 2, windowH / 2);

        paredes = new ArrayList<>();

        initializeDefaultWalls();

        raycasts = new ArrayList<>();
        collisions = new ArrayList<>();
        reflections = new ArrayList<>();
        polygon = new ArrayList<>();
        
        generateRaycasts();
        
        addMouseMotionListener(this);
        addMouseListener(this);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    toggleDrawingMode();
                }
                
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    deleteLastLine();
                }
                
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleReflection();
                }
                
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (numRaycasts < 360) {
                        numRaycasts++;
                        generateRaycasts();
                        repaint();
                    }
                }
                
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (numRaycasts > 0) {
                        numRaycasts--;
                        generateRaycasts();
                        repaint();
                    }
                }
                
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (initialAngle > 0.0) {
                        initialAngle -= 12 * (Math.PI / 180.0);
                        generateRaycasts();
                        repaint();
                    } else {
                        initialAngle = 360.0;
                    }
                }
                
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (initialAngle < 360.0) {
                        initialAngle += 12 * (Math.PI / 180.0);
                        generateRaycasts();
                        repaint();
                    } else {
                        initialAngle = 0.0;
                    }
                }
                
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    if (lightAngle < 360.0) {
                        lightAngle += 1.0;
                        generateRaycasts();
                        repaint();
                    }
                }
                
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    if (lightAngle > 0.0) {
                        lightAngle -= 1.0;
                        generateRaycasts();
                        repaint();
                    }
                }
                
                if (e.getKeyCode() == KeyEvent.VK_H) {
                    togglePolygon = !togglePolygon;
                    generateRaycasts();
                    repaint();
                }
                
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    if (togglePolygon) {
                        togglePolygonColor = !togglePolygonColor;
                        repaint();
                    }
                }
            }
        });
        
        setFocusable(true);
    }
    
    private void toggleReflection() {
        reflect = !reflect;
        generateRaycasts();
        repaint();
    }
    
    private void deleteLastLine() {
        // Remove except default walls
        if (paredes.size() > 4) {
            paredes.removeLast();
            generateRaycasts();
            repaint();
        }
    }

    private void initializeDefaultWalls() {
        int offset = 25;
        
        paredes.add(new Line(new Point(offset, offset), new Point(windowW - offset, offset))); // TOP
        paredes.add(new Line(new Point(windowW - offset, offset), new Point(windowW - offset, windowH - offset * 2))); // RIGHT
        paredes.add(new Line(new Point(windowW - offset, windowH - offset * 2), new Point(offset, windowH - offset * 2))); // BOTTOM
        paredes.add(new Line(new Point(offset, windowH - offset * 2), new Point(offset, offset))); // LEFT
    }

    private void toggleDrawingMode() {
        drawingMode = !drawingMode;
        
        if (!drawingMode) {
            startPoint = null;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!drawingMode) {
            centro.setX(e.getX());
            centro.setY(e.getY());
            generateRaycasts();
        }
        
        currentMousePosition = new Point(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, windowW, windowH);
        
        for (int i = 0; i < reflections.size(); i++) {
            if (collisions.get(i) != null) {
//                g2d.setColor(Color.BLUE.darker());
//                g2d.drawOval((int) collisions[i].getX() - 5,
//                        (int) collisions[i].getY() - 5,
//                        10,
//                        10);

                if (reflections.get(i) != null && reflect) {
                    g2d.setColor(Color.WHITE.darker());
                    g2d.drawLine(
                            (int) reflections.get(i).getP1().getX(),
                            (int) reflections.get(i).getP1().getY(),
                            (int) reflections.get(i).getP2().getX(),
                            (int) reflections.get(i).getP2().getY()
                    );
                }
            }
        }
        
        if (togglePolygonColor && togglePolygon) {
            g2d.setColor(Color.WHITE);
            
            //Just messing around with gradient to fake a light source
//            if (currentMousePosition != null) {
//                float cx = (float) currentMousePosition.getX();
//                float cy = (float) currentMousePosition.getY();
//                
//                RadialGradientPaint radialGradient = new RadialGradientPaint(
//                        cx, cy, radius / 4,
//                        new float[]{0f, 1f},
//                        new Color[]{Color.WHITE, Color.BLACK}
//                );
//                
//                g2d.setPaint(radialGradient);
//            }
            
            int num = polygon.size();
            int[] xp = new int[num], yp = new int[num];
            
            for (int i = 0; i < num; i++) {
                xp[i] = (int) polygon.get(i).getP1().getX();
                yp[i] = (int) polygon.get(i).getP1().getY();
            }
            
            g2d.fillPolygon(xp, yp, num);
        } else {
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < numRaycasts; i++) {
                g2d.drawLine(
                        (int) raycasts.get(i).getP1().getX(),
                        (int) raycasts.get(i).getP1().getY(),
                        (int) raycasts.get(i).getP2().getX(),
                        (int) raycasts.get(i).getP2().getY()
                );
            }
        }
        
        if (!togglePolygonColor) {
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < polygon.size(); i++) {
                g2d.drawLine(
                        (int) polygon.get(i).getP1().getX(),
                        (int) polygon.get(i).getP1().getY(),
                        (int) polygon.get(i).getP2().getX(),
                        (int) polygon.get(i).getP2().getY()
                );
            }
        }

        if (!togglePolygon) {
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < paredes.size(); i++) {
                if (paredes.get(i) == null) {
                    continue;
                }

                g2d.drawLine(
                        (int) paredes.get(i).getP1().getX(),
                        (int) paredes.get(i).getP1().getY(),
                        (int) paredes.get(i).getP2().getX(),
                        (int) paredes.get(i).getP2().getY()
                );
            }
        }

        if (drawingMode && startPoint != null && currentMousePosition != null) {
            g2d.setColor(Color.RED);
            g2d.drawOval(
                    (int) startPoint.getX() - 5,
                    (int) startPoint.getY() - 5,
                    10,
                    10
            );
            
            g2d.drawLine(
                    (int) startPoint.getX(),
                    (int) startPoint.getY(),
                    (int) currentMousePosition.getX(),
                    (int) currentMousePosition.getY()
            );
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (drawingMode) {
            if (startPoint == null) {
                startPoint = new Point(e.getX(), e.getY());
            } else {
                Point endPoint = new Point(e.getX(), e.getY());
                paredes.add(new Line(startPoint, endPoint));
                startPoint = null;
                
                generateRaycasts();
            }
            
            repaint();
        }
    }

    private void generateRaycasts() {
        raycasts.clear();
        collisions.clear();
        reflections.clear();
        polygon.clear();
        
        double angleStep = lightAngle / numRaycasts;

        for (int i = 0; i < numRaycasts; i++) {
            double angle = initialAngle + Math.toRadians(i * angleStep);

            double dx = Math.cos(angle) * radius;
            double dy = Math.sin(angle) * radius;

            Point end = new Point(centro.getX() + dx, centro.getY() + dy);
            raycasts.add(new Line(centro, end));
            
            //Raycasts always send null
            Line collisionLine = findClosestCollision(raycasts.get(i), null);

            if (collisionLine != null) {
                collisions.add(collisionLine.getCollisionPoint());
                
                if (reflect) {
                    reflections.add(calculateReflection(raycasts.get(i), collisionLine));
                }
                
                raycasts.get(i).setP2(collisions.get(i));
                
//                System.out.println("reflection: "+i+", P1: "+reflections[i].getP1().toString());
//                System.out.println("P2: "+reflections[i].getP2().toString());
            } else {
                collisions.add(null);
                reflections.add(null);
            }
        }
        
        if (togglePolygon) {
            generatePolygon();
        }
    }
    
    private void generatePolygon() {
        if (numRaycasts >= 1) {
            for (int i = 0; i < raycasts.size() - 1; i++) {
                polygon.add(new Line(raycasts.get(i).getP2(), raycasts.get(i + 1).getP2()));
            }
            
            //Conect end with beginning
            polygon.add(new Line(raycasts.get(raycasts.size() - 1).getP2(), raycasts.get(0).getP2()));
        }
    }

    private Line findClosestCollision(Line ray, Line currentWall) {
        Line closestCollision = null;
        double closestDistance = Double.MAX_VALUE;
        
        Point p1 = ray.getP1();

        for (Line parede : paredes) {
            if (parede == null) {
                continue;
            }
            
            //Check to see if closest line is not current collided line
            if (currentWall != null) {
                if (parede.compareLines(currentWall)) {
                    continue;
                }
            }
            
            Point collision = checkCollision(ray, parede);
            
            if (collision != null) {
                double distance = distanceBetween(p1, collision);
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    
                    closestCollision = new Line(parede);
                    closestCollision.setCollisionPoint(collision);
                }
            }
        }

        return closestCollision;
    }

    private double distanceBetween(Point p1, Point p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Function that calculates the reflection of a ray after collision
    private Line calculateReflection(Line ray, Line collisionLine) {
        Point collisionPoint = collisionLine.getCollisionPoint();  // Extracts the collision point from the line

        // Raycast vector (incident ray)
        double vx = ray.getP2().getX() - ray.getP1().getX();
        double vy = ray.getP2().getY() - ray.getP1().getY();

        // Retrieves the pre-calculated normal vector
        double nx = collisionLine.getNx();
        double ny = collisionLine.getNy();

        // Dot product between the incident vector and the normal vector
        double dotProduct = vx * nx + vy * ny;

        // Reflected vector
        double reflectedVx = vx - 2 * dotProduct * nx;
        double reflectedVy = vy - 2 * dotProduct * ny;

        // Defines the end point of the reflected line
        Point reflectionEnd = new Point(collisionPoint.getX() + reflectedVx, collisionPoint.getY() + reflectedVy);
        Line reflected = new Line(collisionPoint, reflectionEnd);

        // Finds the closest collision, ignoring the current line
        Line collided = findClosestCollision(reflected, collisionLine);

        if (collided != null) {
            reflected.setP2(collided.getCollisionPoint());
        }

        return reflected;
    }

    private Point checkCollision(Line lineA, Line lineB) {
        double x1 = lineA.getP1().getX();
        double y1 = lineA.getP1().getY();
        double x2 = lineA.getP2().getX();
        double y2 = lineA.getP2().getY();

        double x3 = lineB.getP1().getX();
        double y3 = lineB.getP1().getY();
        double x4 = lineB.getP2().getX();
        double y4 = lineB.getP2().getY();

        double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denominator == 0) {
            return null;
        }

        double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denominator;
        double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denominator;

        if (isBetween(x1, x2, intersectX) && isBetween(y1, y2, intersectY)
                && isBetween(x3, x4, intersectX) && isBetween(y3, y4, intersectY)) {
            return new Point(intersectX, intersectY);
        }

        return null;
    }

    private boolean isBetween(double a, double b, double c) {
        double tolerance = 1e-9;
        return (c >= Math.min(a, b) - tolerance && c <= Math.max(a, b) + tolerance);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Raycast Reflection");
        ReflectiveRaycasting panel = new ReflectiveRaycasting();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setResizable(false);
        frame.setSize(windowW, windowH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
