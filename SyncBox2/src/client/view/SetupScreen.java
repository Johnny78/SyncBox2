	package client.view;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

	/**
	 * Displays a GUI prompt for the user to enter a password
	 * @author John
	 *
	 */
	public class SetupScreen extends JPanel implements ActionListener {

		private static final long serialVersionUID = 1L;
		private JFrame parentFrame;
		private JPasswordField passwordField;
		private JTextField emailField;
		private String password;
		private String email;
		private boolean entered = false;
		
		public SetupScreen(){
			JFrame frame = new JFrame("SyncBox Setup");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			PassPromptCons(frame);
		}
		
		public void PassPromptCons(JFrame f){
			parentFrame = f;
			
			passwordField = new JPasswordField(10);
	        passwordField.setActionCommand("ok");
	        passwordField.addActionListener(this);
	        
	        StyleContext sc = new StyleContext();
	        DefaultStyledDocument doc = new DefaultStyledDocument(sc);
	        final Style heading2Style = sc.addStyle("Heading2", null);
	        heading2Style.addAttribute(StyleConstants.FontSize, new Integer(22));
	        heading2Style.addAttribute(StyleConstants.FontFamily, "sans-serif");
	        heading2Style.addAttribute(StyleConstants.Bold, new Boolean(true));	     
	        StyleConstants.setAlignment(heading2Style, StyleConstants.ALIGN_CENTER);
	        String title = "Thank you for installing SyncBox\n";
	    	        try {
	    				doc.insertString(0,  title,  null);
	    				doc.setParagraphAttributes(0, 1, heading2Style, false);
	    			} catch (BadLocationException e) {e.printStackTrace();}
	        
	        Style TextStyle = sc.addStyle("I'm a Style", null);
	        TextStyle.addAttribute(StyleConstants.FontSize, new Integer(18));
	        TextStyle.addAttribute(StyleConstants.FontFamily, "serif");
	        String text = "So that you can get started securing your documents"
	        		+ " enter the login details you provided on the SyncBox website.\n\n";        		
	        try { doc.insertString(doc.getLength(), text, TextStyle); }
	        catch (BadLocationException e){}
	        
	        Style linkStyle = sc.addStyle("I'm a Style0", null);
	        linkStyle.addAttribute(StyleConstants.FontSize, new Integer(14));
	        linkStyle.addAttribute(StyleConstants.FontFamily, "sans-serif");
	        StyleConstants.setAlignment(linkStyle, StyleConstants.ALIGN_CENTER);
	        linkStyle.addAttribute(StyleConstants.Bold, new Boolean(true));
	        String link = "                                                              http://SyncBox.no-ip.biz:8080/syncbox/";       		
	        try { doc.insertString(doc.getLength(), link, linkStyle); }
	        catch (BadLocationException e){}


	        
	        
	        JTextPane pane = new JTextPane(doc);
	        pane.setEditable(false);
	        pane.setBorder(BorderFactory.createCompoundBorder(
	                pane.getBorder(), 
	                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	 
	        JLabel label = new JLabel("Enter the password: ");
	        label.setLabelFor(passwordField);
	        
	        emailField = new JTextField(20);
	        JLabel label1 = new JLabel("Enter your email: ");
	 
	        JComponent buttonPane = createButton();
	 
	        //Lay out everything.
	        JPanel textPane = new JPanel(new GridLayout(2, 1));        
	        textPane.add(new JScrollPane(pane));
	        
	        JPanel emailPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	        emailPane.add(label1);
	        emailPane.add(emailField);
	        emailPane.add(label);
	        emailPane.add(passwordField);
	        emailPane.add(buttonPane);
	        textPane.add(emailPane);
   
	        add(textPane);

	        
	        this.setOpaque(true); //content panes must be opaque
	        parentFrame.setLocationRelativeTo(null);
	        parentFrame.setContentPane(this);
	        parentFrame.pack();
	        parentFrame.setVisible(true);
		}
		
		 protected JComponent createButton() {
		        JPanel p = new JPanel(new GridLayout(0,1));
		        JButton okButton = new JButton("OK");
		        okButton.setActionCommand("ok");
		        okButton.addActionListener(this);
		        p.add(okButton);
		        return p;
		    }
		
		public void close(){
			parentFrame.setVisible(false);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			 
	        if ("ok".equals(cmd)) {
	        	password = String.valueOf(passwordField.getPassword());
	        	email = emailField.getText();
	        	entered = true;
	        }
		}
		

		

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
		
		private boolean isEntered() {
			return entered;
		}

		public String[] getCredentials(){
			while(!isEntered()){
				Thread.yield();
				try {
					Thread.sleep(1000);				//wait for user to type password
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			String[] creds = {getEmail(), getPassword()};
			return creds;

		}
	}
