package awt.table.gui;
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

/* 
 * ColorRenderer.java (compiles with releases 1.2, 1.3, and 1.4) is used by 
 * TableDialogEditDemo.java.
 */

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import bacnet.table.core.ColorMapperRCP;
import bacnet.table.core.ColorMapperRCPList;

public class HeatMapStringRenderer extends JLabel	implements TableCellRenderer {
	
	private static final long serialVersionUID = -2398479625198709031L;

	private ColorMapperRCPList colorMappers;
	
	public HeatMapStringRenderer(ColorMapperRCPList colorMappers) {
		this.colorMappers = colorMappers;
		//setOpaque(true); //MUST do this for background to show up.
	}

    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus,int row, int column) {
	    String text = (String) value;
	    String header = table.getColumnName(column);
	    
	    ColorMapperRCP colorMapper = colorMappers.getFirstMapper();
	    setFont(colorMapper.getFontString());
	    
//	    String newsRNA = text.replace("rli", "");
//	    try{
//		    int id = Integer.parseInt(newsRNA.trim());
////		    if(id>114){
////			    System.out.println("set foreground red "+text);
////			    setForeground(Color.red); 
////		    }
//	    }catch(Exception e){
//	    }
	    
	    if(column!=0){
		    
		    setBackground(Color.gray);
		    setHorizontalAlignment(LEFT);
		    if(colorMapper!=null) setForeground(ColorMapperRCP.swtColorToAwt(colorMapper.getTextHeaderColor()));
//		    if(!text.equals("yo")){
//			    int inocua = Integer.parseInt(text);
//			    switch (inocua) {
//				case 0:
//					setBackground(Color.red);
//					break;
//				case 1:
//					setBackground(Color.orange);
//					break;
//				case 2:
//					setBackground(Color.white);
//					break;
//
//				default:
//					break;
//			}
//		    }
		    //text="";
		    setToolTipText(text);
		    setText(" "+text);
	    }else{
		    
		    setHorizontalAlignment(RIGHT);
		    setForeground(Color.BLACK);
		    setText(text+" ");
	    }
	    
	    return this;
    }
}
