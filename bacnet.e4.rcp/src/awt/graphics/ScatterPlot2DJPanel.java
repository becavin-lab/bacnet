package awt.graphics;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import bacnet.datamodel.dataset.ExpressionMatrix;

public class ScatterPlot2DJPanel extends JPanel{

	private static final long serialVersionUID = 3999074762754447447L;


	private ExpressionMatrix matrix;
	


	private boolean representBC=true;

	private int length;

	public ScatterPlot2DJPanel(ExpressionMatrix matrix,boolean representBC){
		super(null);
		setBackground(Color.WHITE);
		this.matrix = matrix;
		this.representBC = representBC;
	}
	
	public void setData(ExpressionMatrix matrix,boolean representBC){
		this.matrix = matrix;
		this.representBC = representBC;
		repaint();
	}

	@Override
	public void setBounds(int x, int y, int width, int height){
		this.length = Math.min(width, height);
		super.setBounds(x,y,this.length,this.length);
	}

	@Override
	public void paint(Graphics g){

		super.paint(g);

		// axes painting
		g.setColor(Color.BLACK);
		g.drawLine(length/2, 0, length/2, length);
		for(int i = 0  ; i <= 50; i++){
			g.drawLine(length/2-2, length/2-10*i, length/2+2, length/2-10*i);
		}
		for(int i = 0  ; i <= 50; i++){
			g.drawLine(length/2-2, length/2+10*i, length/2+2, length/2+10*i);
		}

		g.setColor(Color.BLACK);
		g.drawLine(0, length/2, length, length/2);
		for(int i = 0  ; i <= 50; i++){
			g.drawLine(length/2-10*i, length/2-2, length/2-10*i, length/2+2);
		}
		for(int i = 0  ; i <= 50; i++){
			g.drawLine(length/2+10*i, length/2-2, length/2+10*i, length/2+2);
		}

		


		// rescale of the coordinates
		double minX = 0;
		double maxX = 0;
		double minY = 0;
		double maxY = 0;
		for(int i = 0 ; i < matrix.getNumberRow(); i++){
			minX = Math.min(minX, matrix.getValue(i, 0));
			maxX = Math.max(maxX, matrix.getValue(i, 0));
			minY = Math.min(minY, matrix.getValue(i, 1));
			maxY = Math.max(maxY, matrix.getValue(i, 1));
		}

		double max1 = Math.max(Math.abs(minX), Math.abs(maxX));
		double max2 = Math.max(Math.abs(minY), Math.abs(maxY));
		double maxmax = Math.max(max1, max2);
		System.out.println("Max Value of the representation: "+maxmax);
		for(int i = 0 ; i < matrix.getNumberRow(); i++){
			matrix.setValue(matrix.getValue(i, 0)*(length/2-10)/maxmax,i,0);
			matrix.setValue(matrix.getValue(i, 1)*(length/2-10)/maxmax,i,1);
		}

		//ExpressionMatrix matrixInfo = ExpressionMatrix.loadTab("D:/Etude psychometrique/data.txt", true);

		//System.out.println(matrixInfo.getValueAnnotation("OBS 33", "SEXE"));
		
		// Display of points
		for(int i = 0 ; i < matrix.getNumberRow(); i++){
			int x = (int)(matrix.getValue(i, 0)+length/2);
			int y = (int)(matrix.getValue(i, 1)+length/2);
			
			
			if(representBC){
				System.out.println(matrix.getRowName(i));
				String name = matrix.getRowName(i);
				//int age = Integer.parseInt(matrixInfo.getValueAnnotation(matrix.getRowName(i), "AGE"));
				
				// Sexe: blue = Men  red = women
				Color color = Color.blue;
				//if(matrixInfo.getValueAnnotation(matrix.getRowName(i), "SEXE").equals("F")) color = Color.red;
				g.setColor(color);
				
				// Expert = circle ,  Non Expert = square
//				if(matrixInfo.getValueAnnotation(matrix.getRowName(i), "E/NE").equals("E")) g.fillOval(x,y,age/5,age/5);
//				else g.fillRect(x,y,age/5,age/5);
				
				 g.fillOval(x,y,10,10);
				
				
				g.drawString(name, x, y);
				
			} else {
				String name = matrix.getRowName(i);
				if(name.contains("L1")){
					g.setColor(Color.black);
				}
				else if(name.contains("L2")){
					g.setColor(Color.blue);
				}
				else if(name.contains("L3")){
					g.setColor(Color.orange);
					
				}
				else{
					g.setColor(Color.red);
					g.drawString(name, x, y);
				}
//				if(name.equals("10403S-L2-85")){
//					g.setColor(Color.orange);
//					g.drawString(matrix.getRowName(i), x, y);
//				}
//				else if(name.equals("EGD-c-L2-12")){
//					g.setColor(Color.red);
//					g.drawString(matrix.getRowName(i), x, y);
//				}
//				else if(name.equals("EGD-e-L2-35")){
//					g.setColor(Color.blue);
//					g.drawString(matrix.getRowName(i), x, y);
//				}
//				else g.setColor(Color.black);
				g.fillOval(x,y,10,10);
			}
			
			//g.setColor(Color.BLACK);
			//g.drawString(""+matrix.getRowName(i), x, y);
			
			
		}
	}


}
