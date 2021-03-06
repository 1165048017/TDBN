/*
 * This program demonstrates the motion generation by Temporal Deep Belief Net.
 * Even though there are only walking and running motions for training data,
 * TDBN can generate gait transition motions.
 * 
 * Requirements: Java
 * 
 * core.jar is a library from Processing (www.processing.org).
 * Training motions are obtained from CMU database (http://mocap.cs.cmu.edu/).
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import processing.core.PApplet;

public class demo extends PApplet {
	TDBN net;
	PlayerSkel skel;
	int frame = 0;
	int control = 1; // 1 for run, 0 for run
	String testdata[][];
	boolean testMode = false;
	
	// motions used in training. First half is walking, and last half is running
	String training_files[] = {
			"../data/35_01.amc",
			"../data/35_02.amc",
			"../data/35_03.amc",
			"../data/35_04.amc",
			"../data/35_05.amc",
			"../data/35_17.amc",
			"../data/35_18.amc",
			"../data/35_20.amc",
			"../data/35_21.amc",
			"../data/35_22.amc"
	};
	double rotX = 0.5;
	double rotY = 0.5;
	
    public void setup()
    {
		
		try {
			skel = new PlayerSkel("../data/35.asf");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			exit();
		}
		
		if(testMode){
			try {
				// for testing the player
				testdata = loadAMC("../data/35_01.amc");
			} catch (IOException e) {
				e.printStackTrace();
				exit();
			}
		}else{
			if(net == null){
				try {
					net = new TDBN(training_files);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					exit();
				}

				net.train(200); // train the lower RBMs
				net.train2(40); // train the upper CRBM

				net.initRealtime();
			}
		}

		size(900,700,P3D);
		frameRate(30);
		sphereDetail(5);
		noSmooth();
    }    

    public void draw()
    {
    	background(255);

    	// draw activity of hidden units
    	strokeWeight(1);
    	fill(200,200,200);
    	rect(0, 0, net.outputhidden2.length, 700);
    	int y = 20;
    	textSize(18);
    	fill(0,0,0);
    	text("control layer",210,y+10);
    	for(int j=0; j<net.outputhidden2.length; j++){
    		for(int i=0; i<net.outputhidden2[j].length; i++){
        		stroke((int)(net.outputhidden2[j][i] * 255));
        		point(j,i+y);
        	}
    	}
    	y += net.outputhidden2[0].length + 50;
    	fill(0,0,0);
    	text("hidden layers",210,y+70);
    	for(int k=0; k<net.outputhidden.length; k++){
    		for(int j=0; j<net.outputhidden[k].length; j++){
        		for(int i=0; i<net.outputhidden[k][j].length; i++){
            		stroke((int)(net.outputhidden[k][j][i] * 255));
            		point(j,i+y);
            	}
        	}        	
    		y += net.outputhidden[k][0].length + 5;
    	}
    	y += 40;
    	fill(0,0,0);
    	text("visible layer",210,y+30);
    	for(int j=0; j<net.output.length; j++){
    		for(int i=0; i<net.output[j].length; i++){
        		stroke((int)((net.output[j][i]*0.1+0.5) * 255));
        		point(j,i+y);
        	}
    	}
    	
    	
    	// draw body
    	stroke(100);
		strokeWeight(10);
		translate(width/2,3 * height/4,0);
    	
    	rotateX((float) (PI/2.0));
    	rotateY((float) (PI/2.0));
    	rotateZ((float) (PI/2.0));

    	translate(0,200,0);

    	// change the view-angle by mouse drag
    	rotateY((float) ((rotY - 0.5) * PI * 1.5));
    	rotateZ((float) ((rotX - 0.5) * PI * 1.5));

    	fill(255,0,0);
    	
    	if(testMode){
    		// just play one of training motions
        	if(testdata[frame][0] == null) frame = 0;
        	skel.draw(this,testdata[frame]);
        	frame++;    		
    	}else{
    		// generate each frame by TDBN
    		skel.draw(this,net.stepRealtime(control));
    	}
    } 
    
    // loads .amc file into a string array
    String[][] loadAMC(String path) throws IOException{
    	BufferedReader amc = new BufferedReader(new FileReader(path));
    	String line;
    	String data[][] = new String[10000][100];
    	int frame = -1;
    	int boneCnt = 0;
    	while(true){
    		line = amc.readLine();
    		if(line == null) break;
    		if(line.charAt(0) == '#') continue;
    		if(line.charAt(0) == ':') continue;
    		if(line.split(" ").length == 1) continue;
    		if(line.startsWith("root")){
    			boneCnt = 0;
    			frame++;
    		}
    		data[frame][boneCnt] = line;
    		boneCnt++;
    	}
    	
    	return data;
    }
    
    public void mouseDragged(){
    	rotY = 1.0 * mouseX/width;
    	rotX = 1.0 * mouseY/width;
    }
    
    // change the control value by keyboard
    public void keyPressed() {
    	if(key == 'r') {
    		control = 1; // for running
    	}
    	else if(key == 'w') {
    		control = 0; // for walking
    	}
    }

}
