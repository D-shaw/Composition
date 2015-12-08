import java.awt.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.swing.event.*;

public class Piano {
	
	JTabbedPane tabbedPane;
	JFrame mainFrame;	// mainFrame frame
	JFrame nameFrame;	// dialog for name input
	JFrame theFrame;	// tab1
	JFrame chatFrame;	// tab2
	JFrame pianoFrame;	// tab3
	JPanel mainFramePanel;
	ArrayList<JRadioButton> radioButtonList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	String userName;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	Sequencer sequencer2;
	Sequence sequence2;
	Track track2;
	
	JList incomingList;
	JTextField userMessage;
	int nextNum;
	Vector<String> listVector = new Vector<String>();
	HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();
	Sequence mySequence = null;
	
	JList chatMessageList;
	JTextField myWords;
	Vector<String> chatVector = new Vector<String>();
	
	int keys = 24;
	int tempos = 32;
	
	int[] keytunes = new int[24];	
	
	String[] instrumentNames = {"B*", "A#*", "A*", "G#*", "G*", "F#*", "F*", "E*", "D#*", "D*", "C#*", "C*", 
			"B", "A#", "A", "G#", "G", "F#", "F", "E", "D#", "D", "C#", "C"};
	
	
	// Main Method
	public static void main(String[] args) {
		new Piano().startUp();
	}
	
	public void startUp() {
		for (int i = 0; i < 24; i++) {
			keytunes[i] = 83 - i;	// from 83 "B*" to 60 "C"
		}
		try {
			Socket sock = new Socket("54.213.222.107", 4242);
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
			Thread remote = new Thread(new RemoteReader());
			remote.start();
		} catch (Exception ex) {
			System.out.println("couldn't connect - you'll have to play alone.");
		}
		buildGUI();
		setUpMidi();
		setUpPiano();
	}
	
	public void buildGUI() {
		buildGUINameFrame();
		buildGUITempoFrame();
		buildGUIChatFrame();
		buildGUIPianoFrame();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Composition", theFrame.getContentPane());
		tabbedPane.addTab("Piano", pianoFrame.getContentPane());
		tabbedPane.addTab("Chat", chatFrame.getContentPane());

		mainFrame = new JFrame(userName + "'s Piano");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setSize(800,400);
		mainFrame.add(tabbedPane);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
	
	public void buildGUINameFrame() {
		nameFrame = new JFrame();
	    Object result = JOptionPane.showInputDialog(nameFrame, "Enter printer name:");
	    userName = (String) result;
	    
	    int state = 0;
	    try {
	    	out.writeObject(state);
	    	out.writeObject(userName);
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	}
	
	public void buildGUITempoFrame() {
	    
		theFrame = new JFrame(userName + "'s Piano");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		radioButtonList = new ArrayList<JRadioButton>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton clear = new JButton("Clear");
		clear.addActionListener(new MyClearListener());
		buttonBox.add(clear);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton sendIt = new JButton("Send");
		sendIt.addActionListener(new MySendListener());
		buttonBox.add(sendIt);
		
		userMessage = new JTextField();
		buttonBox.add(userMessage);	
		
		incomingList = new JList();
		incomingList.addListSelectionListener(new MyListSelectionListener());
		incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane theList = new JScrollPane(incomingList);
		buttonBox.add(theList);
		incomingList.setListData(listVector);
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add((new JLabel(new ImageIcon("resources/piano.jpg"))));
		
		theFrame.getContentPane().add(background);
		

		GridLayout grid = new GridLayout(keys + 1, tempos + 1);
		grid.setVgap(1);
		grid.setHgap(2);
		mainFramePanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainFramePanel);
		
		UIManager.put("RadioButton.focus", Color.RED);
		
		mainFramePanel.add(new JLabel(" "));
		for (int i = 0; i < tempos; i++) {
			JLabel tempo = new JLabel(" " + String.valueOf(i + 1));
			mainFramePanel.add(tempo);
		}
		for (int i = 0; i < keys; i++) {
			for (int j = 0; j < tempos + 1; j++) {
				if (j == 0) {
					mainFramePanel.add(new JLabel(instrumentNames[i]));
				} else {
					JRadioButton c = new JRadioButton();
					c.setSelected(false);
					radioButtonList.add(c);
					mainFramePanel.add(c);
				}
			}
		}
	}
	
	public void buildGUIChatFrame() {
		chatFrame = new JFrame(userName + "'s Chatting Room");
		chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		chatMessageList = new JList();
		JScrollPane theList = new JScrollPane(chatMessageList);
		buttonBox.add(theList);
		chatMessageList.setListData(chatVector);
		
		myWords = new JTextField();
		buttonBox.add(myWords);
		
		JButton sendChat = new JButton("Send");
		sendChat.addActionListener(new MySendChatListener());
		buttonBox.add(sendChat);
		
		background.add(BorderLayout.CENTER, buttonBox);
		
		chatFrame.getContentPane().add(background);
	}
	
	public void buildGUIPianoFrame() {
		pianoFrame = new JFrame();
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("resources/pianoboard2.jpg"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Image image = img.getScaledInstance(1265, img.getHeight(), Image.SCALE_SMOOTH);
	    ImageIcon imageIcon = new ImageIcon(image);
	    JLabel imageLabel = new JLabel(imageIcon);
		pianoFrame.setContentPane(imageLabel);
		
		imageLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				Point mousePos = event.getPoint();
//				System.out.println("Mouse Pos:");
				if (mousePos.y > 190 && mousePos.y < 440) {
					int key = pianoKey(mousePos.x, mousePos.y);
//					System.out.println(key);
					playPiano(key);
//					System.out.println(mousePos.x);
//					System.out.println(mousePos.y);
				}
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
	}
	
	public void setUpMidi() {
		try {
			sequencer = MidiSystem.getSequencer();  // Sequencer acts as player
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);  // Track lived in Sequence
			track = sequence.createTrack();  // Midi data lives in track
			sequencer.setTempoInBPM(120);
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 0.5));		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void buildTrackAndStart() {
		
		int[] trackList = null;
		
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for (int i = 0; i < keys; i++) {
			trackList = new int[tempos];
			int key = keytunes[i];	// which key tune this is 
			
			// tell if a beat is checked or not
			for (int j = 0; j < tempos; j++) {
				JRadioButton jc = radioButtonList.get(j + (tempos * i));
				if (jc.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			
			makeTracks(trackList);	// add this instrument's row's tempos beats
		}
		
		track.add(makeEvent(192, 1, 1, 0, 31));  // make sure beat 31 has event. then starts over.
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// add one instrument row's 16 beats as events
	public void makeTracks(int[] trackList) {
		for (int i = 0; i < tempos; i++) {
			int key = trackList[i];
			if (key != 0) {
				track.add(makeEvent(144, 1, key, 100, i));  // NoteOn
				track.add(makeEvent(128, 1, key, 100, i + 1));  // NoteOff
			}
		}
	}
	
	/**
	 * @param comd: message type. 144 means Note on, 128 means Note Off; 176 means ControllerEvent; 192 means PROGRAM_CHANGE.
	 * @param chan: channel. like musician in a bank.
	 * @param note: note to play. from 0 to 127.
	 * @param vel: velocity. how fast and hard. 100 is good default.
	 * @param tick: time this MidiEvent happens. tick difference between NoteOn and NoteOff is duration.
	 */
	public MidiEvent makeEvent(int comd, int chan, int note, int vel, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, note, vel);
			event = new MidiEvent(a, tick);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}
	
	// Set up Sequence for Piano tab3
	public void setUpPiano() {
		try {
			sequencer2 = MidiSystem.getSequencer();
			sequencer2.open();
			sequence2 = new Sequence(Sequence.PPQ, 4);
			track2 = sequence2.createTrack();
			sequencer2.setTempoInBPM(120);
			float tempoFactor = sequencer2.getTempoFactor();
			sequencer2.setTempoFactor((float) (tempoFactor * 0.5));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// This is for the piano to tell which piano note the mouse is click on.
	// "D" length is 29 pixels, the other key's length is 26. 
	public int pianoKey(int x, int y) {
		int key = 41;	// start from 53 "F3"
		int[] whiteKeys = {0, 2, 4, 6, 7, 9, 11};
		if (y > 330) {	// must be white key
			int numOfKey = x / 45;
//			System.out.println("numOfKey: " + numOfKey);
			int numOfOatave = numOfKey / 7;
//			System.out.println("numOfOatave: " + numOfOatave);
			int numOfWhite = numOfKey % 7;
//			System.out.println(numOfWhite);
			int countWhite = whiteKeys[numOfWhite];
			key = numOfOatave * 12 + countWhite + key;
		} else {	// can be either white or black key
			int numOfOatave = x / 315;
//			System.out.println("numOfOatave: " + numOfOatave);
			int oataveX = x % 315;
//			System.out.println("oataveX: " + oataveX);
			if (oataveX < 263) {	// before "D"
				int countKey = oataveX / 26;
//				System.out.println("countKey: " + countKey);
				key = numOfOatave * 12 + countKey + key;
			} else {	// after "D"
				int countKey = (oataveX - 263) / 26 + 10;
//				System.out.println("countKey: " + countKey);
				key = numOfOatave * 12 + countKey + key;
			}
		}
		return key;
	}
	
	// this is for Piano to play
	public void playPiano(int key) {
		track2.add(makeEvent(144, 1, key, 100, 0));
		track2.add(makeEvent(128, 1, key, 100, 1));
		try {
			sequencer2.setSequence(sequence2);
			sequencer2.setLoopCount(0);
			sequencer2.start();
			sequencer2.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sequence2.deleteTrack(track2);
		track2 = sequence2.createTrack();
	}
	
	// listen to start button
	public class MyStartListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			buildTrackAndStart();
		}
	}
	
	// listen to stop button
	public class MyStopListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			sequencer.stop();
		}
	}
	
	// listen to clear button
	public class MyClearListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			sequencer.stop();
			for (int i = 0; i < keys * tempos; i++) {
				radioButtonList.get(i).setSelected(false);
			}
		}
	}
	
	// listen to tempoUp button
	public class MyUpTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 1.03));
		}
	}
	
	// listen to tempoDown button
	public class MyDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * .97));
		}
	}
	
	// listen to sendIt button
	public class MySendListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			boolean[] checkboxState = new boolean[keys * tempos];
			int state = 1;
			for (int i = 0; i < keys * tempos; i++) {
				JRadioButton check = (JRadioButton) radioButtonList.get(i);
				if (check.isSelected()) {
					checkboxState[i] = true;
				}
			}
			try {
				out.writeObject(state);
				out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
				out.writeObject(checkboxState);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			userMessage.setText("");
		}
	}
	
	// listen to Chat tab's Send button
	public class MySendChatListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int state = 2;
			try {
				out.writeObject(state);
				out.writeObject(userName + ": " + myWords.getText());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			myWords.setText("");
		}
	}
	
	public class MyListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent le) {
			if (!le.getValueIsAdjusting()) {
				String selected = (String) incomingList.getSelectedValue();
				if (selected != null) {
					boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
//					System.out.println("retrieve from otherSeqsMap");
					changeSequence(selectedState);
					sequencer.stop();
					buildTrackAndStart();
				}
			}
		}
	}
	
	public void changeSequence(boolean[] checkboxState) {
		for (int i = 0; i < tempos * keys; i++) {
			JRadioButton check = (JRadioButton) radioButtonList.get(i);
			if (checkboxState[i]) {
				check.setSelected(true);
			} else {
				check.setSelected(false);
			}
		}
	}
	
	public class RemoteReader implements Runnable {
		boolean[] checkboxState = null;
		Object obj = null;
		public void run() {
			try {
				while ((obj = in.readObject()) != null) {
					int state = (int) obj;
					if (state == 1) {	// receive a Piano
						System.out.println("got a Piano from server");
						String nameToShow = (String) in.readObject();
						checkboxState = (boolean[]) in.readObject();
						otherSeqsMap.put(nameToShow, checkboxState);
						listVector.add(nameToShow);
						incomingList.setListData(listVector);
					} else {	// receive a Chat Message or user join/leave message
						String chatMessage = (String) in.readObject();
						chatVector.add(chatMessage);
						chatMessageList.setListData(chatVector);
					}		
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
