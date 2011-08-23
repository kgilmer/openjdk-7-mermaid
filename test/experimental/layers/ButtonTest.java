import java.awt.*;

public class ButtonTest{
    public static final void main(String args[]){
	Frame f = new Frame();
	f.setBounds(100,100,400,400);
	f.setLayout(new BorderLayout());
	f.add(new Button("I'm AWT button. Don't resize me!"));
	f.setVisible(true);
    }
}
