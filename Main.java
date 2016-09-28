
/* Code for Assignment ?? 
 * Name:
 * Usercode:
 * ID:
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** The Main Class
 * 
 * This Class Handles the mechanics of the simulation.  It stores the SCARA arm, the current drawing on
 * the screen and the path of the tool.
 */
public class Main{

    // -------- Fields -------- \\

    // The SCARA Arm simulator
    private Arm arm;

    // The current Drawing on the Screen
    private Drawing drawing;

    // The path that the tool must follow
    private ToolPath tool_path;

    // The State of the GUI
    private int state; 
    // 0 - nothing
    // 1 - inverse point kinematics - point
    // 2 - enter path. Each click adds point  
    // 3 - enter path pause. Click does not add the point to the path

    /**
     * This is the constructor.
     * 
     * It initialises the interface and the fields.
     */
    public Main(){

        // Initialise the User Interface with the window size and divider
        UI.initialise();
        UI.setWindowSize(900, 500);
        UI.setDivider(0.25);

        // Add the Buttons
        UI.addButton("Test Inverse", this::inverse);        // Takes an x,y coordinate and produces the arm positions.
        UI.addButton("Draw Path", this::enter_path_xy);     // Let's the user draw a Path

        // Pre-written Shapes for Assessment
        UI.addButton("Horizontal Line", this::doLine);      // Draws Horizontal Line
        UI.addButton("Square", this::doSquare);             // Draw Square
        UI.addButton("Circle", this::doCircle);             // Draws a circle

        //UI.addButton("Save Draw Coordinates", this::save_xy);        
        //UI.addButton("Load Draw Coordinates", this::load_xy);

        // To save the PWM values
        UI.addButton("Save PWM values", this::save_pwm);
        //UI.addButton("Load PWM values", this::load_pwm);

        // Allows the user to send to the RPi
        UI.addButton("Send to Pi", this::doSend);

        // Allows you to quit the simulation
        UI.addButton("Quit", UI::quit);

        // Allows the user to interact with the program
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);

        //ServerSocket serverSocket = new ServerSocket(22);  // This presumably does something.  Not sure what.

        // Initialise fields
        this.arm = new Arm();
        this.drawing = new Drawing();
        this.tool_path = new ToolPath();

        // Run the Program
        this.run();

    }

    /**
     * Allows key input from the user.
     * 
     * @param action is the key pressed.
     */
    public void doKeys(String action){

        /*# Debugging */
        // Print the key that has been pressed
        //UI.printf("Key :%s \n", action);

        // Change the state of the system to stop drawing lines.
        if (action.equals("b")) { state = 3; }

    }

    /**
     * Allows mouse input from the user.
     * 
     * @param action is the action performed by the mouse
     * @param x is the x coordinate of the mouse
     * @param y is the y coordinate of the mouse
     */
    public void doMouse(String action, double x, double y) {
        /*# Debugging */
        // Print the mouse activity
        //UI.printf("Mouse Click:%s, state:%d  x:%3.1f  y:%3.1f\n", action,state,x,y);

        // Clears the graphics pane.
        UI.clearGraphics();

        // Draws the position of the mouse on the graphics pane
        String out_str = String.format("%3.1f %3.1f",x,y);
        UI.drawString(out_str, x+10,y+10);

        // ---------- Handing Mouse Action -------- \\
        if(action.equals("clicked")){

            // Clicked in State 1 is to test the inverse kinematic equations.
            if(state == 1){
                arm.inverseKinematic(x, y);
                arm.draw();
                return;
            }

            // Clicked in State 2 adds a point to the drawing with the pen down.
            if(state == 2){
                // Add point to drawing
                UI.printf("Adding point x=%f y=%f\n",x,y);

                // If the point is valid.  Add it to the drawing.
                if(arm.inverseKinematic(x,y)){
                    drawing.add_point_to_path(x,y,true);
                }

                // Draw the project
                drawing.draw();
                arm.draw();

                // Print the path.
                drawing.print_path();
                return;
            }

            // Click in State 3 adds a point to the drawing the the pen up.
            if(state == 3){
                // add point and draw
                //UI.printf("Adding point x=%f y=%f\n",x,y);
                drawing.add_point_to_path(x,y,false); // add point wit pen up

                // Calculate Arm Position.
                arm.inverseKinematic(x,y);

                // Draw drawing
                drawing.draw();
                arm.draw();

                drawing.print_path();

                // Change the state to Pen Down.
                state = 2;
                return;
            }

        }

        // When the mouse is moved on the screen update the GUI
        if((state == 2 || state == 3) && action.equals("moved")){
            // Calculate position of arm
            arm.inverseKinematic(x,y);
            arm.draw();

            // draw segment from last entered point to current mouse position (providing at least one point has been drawn)
            if ((state == 2) && (drawing.get_path_size() > 0) ){
                PointXY lp = new PointXY();
                lp = drawing.get_path_last_point();

                // Will only draw line if pen was down from last point.
                if (lp.get_pen()){
                    UI.setColor(Color.GRAY);
                    UI.drawLine(lp.get_x(),lp.get_y(),x,y);
                }
            }

            // Draw the drawing
            drawing.draw();
            return;

        }
    }

    /**
     * To test the inverse kinematic equations
     */
    public void inverse(){
        state = 1;
        arm.draw();
    }

    /**
     * Draws a horizontal line
     */
    public void doLine(){
        // Clear the current Drawing
        drawing = new Drawing();
        UI.clearGraphics();

        int startX = 272;
        int startY = 160;
        int endX = 392;
        int endY = 160;

        // Add two points to the drawing
        drawing.add_point_to_path(startX, startY, true);
        drawing.add_point_to_path(endX, endY, true);

        // Find the last arm position
        arm.inverseKinematic(endX, endY);

        // Draw Project
        drawing.draw();
        arm.draw();

        // Print the path
        drawing.print_path();
        state = 0;
        return;
    }

    /**
     * Draws the square
     */
    public void doSquare(){

        // Clear the current drawing
        drawing = new Drawing();
        UI.clearGraphics();

        int topLeft_x = 290;
        int topLeft_y = 118;
        int size = 84;

        int x = topLeft_x;
        int y = topLeft_y;
        if(arm.inverseKinematic(x, y)){
            drawing.add_point_to_path(x, y, true);
        }
     
        x = topLeft_x + size;
        y = topLeft_y;
        if(arm.inverseKinematic(x, y)){
            drawing.add_point_to_path(x, y, true);
        }
        
        x = topLeft_x + size;
        y = topLeft_y + size;
        if(arm.inverseKinematic(x, y)){
            drawing.add_point_to_path(x, y, true);
        }
     
        
        x = topLeft_x;
        y = topLeft_y + size;
        if(arm.inverseKinematic(x, y)){
            drawing.add_point_to_path(x, y, true);
        }
        
        x = topLeft_x;
        y = topLeft_y;
        if(arm.inverseKinematic(x, y)){
            drawing.add_point_to_path(x, y, true);
        }

        // Draw Project
        drawing.draw();
        arm.draw();

        // Print Path
        drawing.print_path();
        state = 0;
        return;

    }

    /**
     * Calculates the points to draw a perfect circle onto the GUI
     */
    public void doCircle(){
        // Clear the current Drawing
        drawing = new Drawing();
        UI.clearGraphics();

        int centre_x = 332;
        int centre_y = 140;
        double x = 0;
        double y = 0;

        int numPoints = 30;
        double angle = 2*Math.PI/(numPoints-4);     // Minus four so it will slightly overlap for more accuracy

        // Add circular points to the drawing.
        for(int n = 0; n < numPoints; n++){
            x = centre_x + 60*Math.cos(n*angle);
            y = centre_y + 60*Math.sin(n*angle);

            UI.printf("Adding point x=%f y=%f\n",x,y);
            if(arm.inverseKinematic(x, y)) { drawing.add_point_to_path(x, y, true); }
        }

        // Find the arm positions.
        arm.inverseKinematic(x,y);

        // Draw the project
        drawing.draw();
        arm.draw();

        // Print the path.
        drawing.print_path();
        state = 0;
        return;

    }

    public void save_xy(){
        state = 0;
        String fname = UIFileChooser.save();
        drawing.save_path(fname);
    }

    /**
     * For manually entering a path into the system
     */
    public void enter_path_xy(){
        // Clear the current drawing
        drawing = new Drawing();

        // Change the state
        state = 2;

    }

    public void doSend(){
        try{Runtime.getRuntime().exec("terminal");}catch(IOException e){UI.println();}
    }

    public void load_xy(){
        state = 0;
        String fname = UIFileChooser.open();
        drawing.load_path(fname);
        drawing.draw();
        arm.draw();
    }

    // Saves the PWM values into a file.
    public void save_pwm(){
        // Choose a place to save the file
        String fname = UIFileChooser.save();

        // Convert the path into PWM values and save it
        tool_path.convert_drawing_to_angles(drawing, arm, fname);
    }

    public void load_pwm(){
    }

    public void run() {

        // Every 20 milliseconds update the arm position
        while(true) {
            arm.draw();
            UI.sleep(20);
        }
    }

}
