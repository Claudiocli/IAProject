import java.awt.EventQueue;

public class Main {
	public static void main(String[] args) {		
		System.setProperty("sun.java2d.opengl", "True");

		EventQueue.invokeLater(PaperIO::new);
	}
}
