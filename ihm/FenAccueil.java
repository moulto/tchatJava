/**
 * Package contenant toutes les fenêtres de l'application
 */
package ihm;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import client.Administrateur;
import client.Membre;
import client.Utilisateur;

import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import serveur.Canal;
import serveur.CanalPrive;
import serveur.CanalPublic;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JInternalFrame;
import javax.swing.JList;

/**
 * Fenêtre d'accueil de l'utilisateur qui s'est identifié sur l'application.
 * @author STRI
 *
 */
public class FenAccueil {

	/**
	 * Frame principale de la fenêtre
	 */
	private JFrame frmApplicationTchatStri;
	
	/**
	 * Socket de connexion du client.
	 */
	private Socket sockConnexion;
	
	/**
	 * Utilisateur courant de l'application
	 */
	private Utilisateur utilisateur;
	
	/**
	 * Champ de saisie permettant de donner une justification à un statut d'absence
	 */
	private JTextField saiJustification;
	
	/**
	 * Buffer permettant de lire les messages du serveur
	 */
	private BufferedReader lire = null;
	
	/**
	 * Buffer d'écriture pour envoyer des messages au serveur
	 */
	private PrintWriter ecrire;
	
	/**
	 * Fenêtre interne permettant de rejoindre les canaux.
	 */
	private final JInternalFrame intFenRejCanal = new JInternalFrame("Rejoindre un canal");
	
	/**
	 * Correspondance Index et Identifiant d'un canal pour la liste des canaux déjà ouverts
	 */
	private ArrayList<String> correspondanceIndexId;
	
	/**
	 * Ouvre la fenêtre.
	 */
	public void ouvrirFenetre(){
		this.frmApplicationTchatStri.setVisible(true);
	}
	

	/**
	 * Construit une fenêtre d'accueil et affecte les valeurs aux attributs.
	 * @param sockConnexion Socket de connexion du client
	 * @param utilisateur Utilisateur actuel de l'application
	 */
	public FenAccueil(Socket sockConnexion, Utilisateur utilisateur) {
		this.sockConnexion = sockConnexion;
		this.utilisateur = utilisateur;
		initialize();
	}
	
	/**
	 * Ferme la fenêtre
	 */
	public void fermerFenetre(){
		this.frmApplicationTchatStri.setVisible(false);
		this.frmApplicationTchatStri.dispose();
	}
	
	/**
	 * Récupère un message reçu du client
	 * @return Message reçu du client.
	 */
	public String lireMesg(){
		try{
			String message;
			/* On initialise le buffer de lecture pour récupérer la confirmation de connexion de la part du serveur */
			lire = new BufferedReader(new InputStreamReader(this.sockConnexion.getInputStream()));
			/* On récupère le message du serveur */
			message = lire.readLine();
			return message;
		}
		catch (IOException exception){
			return("Impossible de récupérer le message du serveur");
		}
	}
	
	/**
	 * Envoie un message à un client, puis vide le buffer d'écriture.
	 * @param message Message que le serveur veut envoyer au client
	 */
	public void envoyerMesg(String message){
		try{
			ecrire = new PrintWriter(this.sockConnexion.getOutputStream());
			ecrire.println(message);
			ecrire.flush();
		}
		catch(IOException exception){
			System.out.println("Imposible d'envoyer un message au client");
		}
	}
	
	/**
	 * Permet de récupérer et d'afficher dans la liste déroulante la liste des canaux ouverts par l'utilisateur
	 * @param idUtil Identifiant de l'utilisateur de l'application
	 * @return La combo est remplie si tout se passe bien et null sinon
	 */
	public JComboBox initListeCanaux(int idUtil){
		/* On envoie au serveur une demande de récupération des canaux sur lesquels l'utilisateur est connecté */
		envoyerMesg("12");
		/* On regarde si le serveur atteste bonne réception de notre demande */
		if(Integer.parseInt(lireMesg()) == 12){
			/* Serveur OK */
			/* On envoie l'identifiant de l'utilisateur au serveur */
			envoyerMesg(String.valueOf(idUtil));
			/* On récupère la liste des canaux du serveur sous forme idCanal#nomCanal/idCanal#nomCanal */
			String listeCanal = lireMesg();
			if(listeCanal.compareTo("") == 0){
				/* on renvoie une combo vide */
				return(new JComboBox());
			}
			/* On décompose cette liste canal par canal */
			String[] listeCanalDecomp = listeCanal.split("/");
			/* On ajoute les canaux dans la combo prévue à cet effet et on réalise une correspondance idCanal/index dans une ArrayList */
			JComboBox comboCanauxActifs = new JComboBox();
			int i; /* indice de parcours du tableau des canaux */
			this.correspondanceIndexId = new ArrayList<String>(); /* ArrayList qui va permettre la correspondance index/identifiant du canal */
			/* on boucle pour remplir la combo */
			for(i=0;i<listeCanalDecomp.length;i++){
				/* Ajoute le nom du canal à la combo et on réalise la correspondance */
				comboCanauxActifs.addItem(listeCanalDecomp[i].split("#")[1]);
				this.correspondanceIndexId.add(i, listeCanalDecomp[i].split("#")[0]);
			}
			/* on retourne la combo */
			return comboCanauxActifs;
		}else{
			/* Serveur non OK*/
			return null;
		}
	}

	/**
	 * Initialise la fenêtre.
	 */
	private void initialize() {
		frmApplicationTchatStri = new JFrame();
		frmApplicationTchatStri.setTitle("Application Tchat STRI");
		frmApplicationTchatStri.setResizable(false);
		frmApplicationTchatStri.setBounds(100, 100, 1024, 700);
		frmApplicationTchatStri.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JPanel panel = new JPanel();
		frmApplicationTchatStri.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		String statutActuel =  "En ligne";
		final JLabel libBienvenue = new JLabel("Bienvenue "+this.utilisateur.getNom()+" "+this.utilisateur.getPrenom()+" ("+statutActuel+")");
		libBienvenue.setFont(new Font("Liberation Serif", Font.BOLD, 17));
		libBienvenue.setBounds(12, 12, 729, 27);
		panel.add(libBienvenue);
		
		saiJustification = new JTextField();
		saiJustification.setFont(new Font("Liberation Serif", Font.PLAIN, 18));
		saiJustification.setColumns(10);
		saiJustification.setBounds(769, 84, 241, 21);
		panel.add(saiJustification);
		final JLabel libJustification = new JLabel("Justification :");
		libJustification.setFont(new Font("Liberation Serif", Font.BOLD, 17));
		libJustification.setBounds(769, 51, 126, 27);
		panel.add(libJustification);
		final JButton btnJustification = new JButton("Ok");
		btnJustification.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* On vérifie que l'utilisateur n'a pas mis de / ou de # dans sa justification */
				if(saiJustification.getText().contains("#") || saiJustification.getText().contains("/")){
					utilisateur.setJustification(saiJustification.getText());
					libBienvenue.setText("Bienvenue "+utilisateur.getNom()+" "+utilisateur.getPrenom()+" (Absent : "+utilisateur.getJustification()+")");
					/* Requête de modification de statut */
					envoyerMesg("1"); /* on envoie la demande au serveur */
					/* On attend la réponse du serveur qui doit être identique à notre requête */
					if(Integer.parseInt(lireMesg())==1){
						/* Le serveur a répondu favorable, on lui envoie les infos idUtil/statut/justif */
						envoyerMesg(String.valueOf(utilisateur.getId())+"/"+String.valueOf(utilisateur.getStatut())+"/"+utilisateur.getJustification());
						/* On attend sa réponse */
						if(Integer.parseInt(lireMesg())==1){
							/* Réponse favorable */
							JOptionPane.showMessageDialog(panel, "Votre modification de statut a bien été prise en compte","Modification statut",JOptionPane.INFORMATION_MESSAGE);
						}else{
							/* Réponse défavorable */
							JOptionPane.showMessageDialog(panel, "Votre modification de statut a échouée, merci de réessayer ultérieurement","Erreur modification statut",JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		btnJustification.setFont(new Font("Liberation Serif", Font.BOLD, 15));
		btnJustification.setBounds(937, 117, 73, 25);
		panel.add(btnJustification);
		/* Par défaut la justification est invisible */
		saiJustification.setVisible(false);
		btnJustification.setVisible(false);
		libJustification.setVisible(false);
		final JComboBox comboCanauxActifs = initListeCanaux(utilisateur.getId());
		if(comboCanauxActifs != null){
			comboCanauxActifs.setFont(new Font("Liberation Serif", Font.PLAIN, 17));
			comboCanauxActifs.setBounds(12, 123, 275, 24);
			panel.add(comboCanauxActifs);
		}else{
			JOptionPane.showMessageDialog(panel, "Erreur avec le remplissage de la liste des canaux","Erreur remplissage liste des canaux",JOptionPane.ERROR_MESSAGE);
		}
		
		final JComboBox comboStatut = new JComboBox();
		comboStatut.setFont(new Font("Liberation Serif", Font.PLAIN, 17));
		comboStatut.setBounds(843, 14, 167, 27);
		comboStatut.addItem("En ligne");
		comboStatut.addItem("Occupé");
		comboStatut.addItem("Absent");
		//System.out.println(utilisateur.getStatut()+" "+utilisateur.stringStatut(utilisateur.getStatut()));
		comboStatut.setSelectedIndex(utilisateur.getStatut());
		String statutSel = comboStatut.getSelectedItem().toString();
		libBienvenue.setText("Bienvenue "+utilisateur.getNom()+" "+utilisateur.getPrenom()+" ("+statutSel+")");
		if(utilisateur.getStatut() == 2){
			saiJustification.setVisible(true);
			btnJustification.setVisible(true);
			libJustification.setVisible(true);
			saiJustification.setText(utilisateur.getJustification());
			libBienvenue.setText("Bienvenue "+utilisateur.getNom()+" "+utilisateur.getPrenom()+" (Absent : "+utilisateur.getJustification()+")");
		}
		class ListenerCombo implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				String statutSelect = comboStatut.getSelectedItem().toString();
				if(statutSelect.compareTo("En ligne") == 0){
					utilisateur.setStatut(0);
					utilisateur.setJustification("");
					saiJustification.setVisible(false);
					btnJustification.setVisible(false);
					libJustification.setVisible(false);
					libBienvenue.setText("Bienvenue "+utilisateur.getNom()+" "+utilisateur.getPrenom()+" ("+statutSelect+")");
					/* Requête de modification de statut */
					envoyerMesg("1"); /* on envoie la demande au serveur */
					/* On attend la réponse du serveur qui doit être identique à notre requête */
					if(Integer.parseInt(lireMesg())==1){
						/* Le serveur a répondu favorable, on lui envoie les infos idUtil/statut/justif */
						envoyerMesg(String.valueOf(utilisateur.getId())+"/"+String.valueOf(utilisateur.getStatut())+"/"+" ");
						/* On attend sa réponse */
						if(Integer.parseInt(lireMesg())==1){
							/* Réponse favorable */
							JOptionPane.showMessageDialog(panel, "Votre modification de statut a bien été prise en compte","Modification statut",JOptionPane.INFORMATION_MESSAGE);
						}else{
							/* Réponse défavorable */
							JOptionPane.showMessageDialog(panel, "Votre modification de statut a échouée, merci de réessayer ultérieurement","Erreur modification statut",JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				if(statutSelect.compareTo("Occupé") == 0){
					utilisateur.setStatut(1);
					utilisateur.setJustification("");
					saiJustification.setVisible(false);
					btnJustification.setVisible(false);
					libJustification.setVisible(false);
					libBienvenue.setText("Bienvenue "+utilisateur.getNom()+" "+utilisateur.getPrenom()+" ("+statutSelect+")");
					/* Requête de modification de statut */
					envoyerMesg("1"); /* on envoie la demande au serveur */
					/* On attend la réponse du serveur qui doit être identique à notre requête */
					if(Integer.parseInt(lireMesg())==1){
						/* Le serveur a répondu favorable, on lui envoie les infos idUtil/statut/justif */
						envoyerMesg(String.valueOf(utilisateur.getId())+"/"+String.valueOf(utilisateur.getStatut())+"/"+" ");
						/* On attend sa réponse */
						if(Integer.parseInt(lireMesg())==1){
							/* Réponse favorable */
							JOptionPane.showMessageDialog(panel, "Votre modification de statut a bien été prise en compte","Modification statut",JOptionPane.INFORMATION_MESSAGE);
						}else{
							/* Réponse défavorable */
							JOptionPane.showMessageDialog(panel, "Votre modification de statut a échouée, merci de réessayer ultérieurement","Erreur modification statut",JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				if(statutSelect.compareTo("Absent") == 0){
					utilisateur.setStatut(2);
					saiJustification.setVisible(true);
					btnJustification.setVisible(true);
					libJustification.setVisible(true);
				}
			}
		}
		comboStatut.addActionListener(new ListenerCombo());
		panel.add(comboStatut);
		
		JLabel libStatut = new JLabel("Statut :");
		libStatut.setFont(new Font("Liberation Serif", Font.BOLD, 17));
		libStatut.setBounds(769, 12, 64, 27);
		panel.add(libStatut);
		
		intFenRejCanal.setBounds(314, 252, 436, 252);
		panel.add(intFenRejCanal);
		
		final JPanel panel_1 = new JPanel();
		intFenRejCanal.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(null);
		final JButton btnAdmin = new JButton("Administration");
		btnAdmin.setFont(new Font("Liberation Serif", Font.BOLD, 25));
		btnAdmin.setBounds(373, 460, 275, 35);
		btnAdmin.setVisible(false);
		panel.add(btnAdmin);
		/* Si l'utilisateur est administrateur on lui ajoute le bouton d'administration */
		if(this.utilisateur.getGrade() == 2){
			btnAdmin.setVisible(true);
		}
		final JButton btnCompte = new JButton("Gestion du compte");
		btnCompte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* click sur le bouton gestion de compte permet de voir ses informations personnelles et de modifier son mot de passe */
				/* On affiche la fenêtre de compte */
				FenCompte fenCompte = new FenCompte(sockConnexion, utilisateur);
				fenCompte.ouvrirFenetre();
				fermerFenetre();
			}
		});
		final JButton btnCreer = new JButton("Créer un canal");
		final DefaultListModel listCanauxModele = new DefaultListModel();
		
			
		final JButton btnRejoindre = new JButton("Rejoindre un canal");
		btnRejoindre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				/* On cache les boutons pour corriger un bug graphique */
				btnRejoindre.setVisible(false);
				btnCompte.setVisible(false);
				btnCreer.setVisible(false);
				btnAdmin.setVisible(false);
				/* On envoie la requête au serveur */
				envoyerMesg("2");
				/* On regarde que le serveur atteste bonne réception de la requête */
				if(Integer.parseInt(lireMesg()) == 2){
					/* On envoie l'identifiant de l'utilisateur */
					envoyerMesg(String.valueOf(utilisateur.getId()));
					/* On attend la liste des canaux que l'on va décomposer et stocker dans une Map composée de l'identifiant du canal et de son nom */
					final ArrayList<String> canauxDispo = new ArrayList<String>();
					int i; /* indice de parcours de la liste */
					String listeCanaux;
					String[] listeCanauxDecomp;
					int nbCanaux;
					listeCanaux = lireMesg(); /* idCanal#nomCanal/idCanal#nomCanal/... */
					listeCanauxDecomp = listeCanaux.split("/");
					nbCanaux = listeCanauxDecomp.length;
					/* On ajoute les canaux dans la Map */
					for(i=0;i<nbCanaux;i++){
						canauxDispo.add(i, listeCanauxDecomp[i].split("#")[0]);
						/* Au passage on les ajoute à la JList */
						listCanauxModele.addElement(listeCanauxDecomp[i].split("#")[1]);
					}
					/* On fabrique la liste */
					final JList listCanaux = new JList();
					listCanaux.setModel(listCanauxModele);
					listCanaux.setBounds(12, 12, 402, 145);
					
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setBounds(12, 12, 402, 145);
					panel_1.add(scrollPane);
					scrollPane.setViewportView(listCanaux);
					
					JButton btnRejoinde2 = new JButton("Rejoinde");
					btnRejoinde2.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							/* On demande au serveur de rejoindre le canal sélectionné */
							envoyerMesg("8");
							if(Integer.parseInt(lireMesg()) == 8){
								int index = listCanaux.getSelectedIndex();
								/* on vérifie que l'utilisateur a bien selectionné un canal */
								if(index < 0){
									JOptionPane.showMessageDialog(panel, "ERREUR, vous n'avez pas sélectionné de canal","Erreur pas de canal sélectionné",JOptionPane.ERROR_MESSAGE);
								}else{
									/* on envoie au serveur que l'on souhaite rejoindre le canal en lui envoyant l'identifiant du canal */
									envoyerMesg(String.valueOf(utilisateur.getId())+"#"+canauxDispo.get(index));
									/* On attend la réponse du serveur 0 : erreur / 1 : ok utilisateur / 2 : ok modérateur */
									int rep = Integer.parseInt(lireMesg());
									System.out.println("rep ="+rep);
									if(rep == 0){
										/* erreur */
										JOptionPane.showMessageDialog(panel, "ERREUR, impossible de rejoindre le canal","Erreur impossible de rejoindre le canal",JOptionPane.ERROR_MESSAGE);
									}else{
										/* Si c'est ok pour une connexion on récup les infos du canal */
										String infosCanal = lireMesg();
										/* si commence par 1 : privé si par 0 : public */
										if(rep == 1){
											/* ok utilisateur */
											if(Integer.parseInt(infosCanal.split("/")[0])==1){
												CanalPrive canal = new CanalPrive(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null,null);
												try {
													FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, false,true);
													fenCanal.ouvrirFenetre();
													fermerFenetre();
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}else{
												CanalPublic canal = new CanalPublic(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null);
												try {
													FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, false,false);
													fenCanal.ouvrirFenetre();
													fermerFenetre();
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
											
											
										}else{
											/* ok modérateur */
											if(Integer.parseInt(infosCanal.split("/")[0])==1){
												CanalPrive canal = new CanalPrive(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null,null);
												try {
													FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, true,true);
													fenCanal.ouvrirFenetre();
													fermerFenetre();
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}else{
												CanalPublic canal = new CanalPublic(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null);
												try {
													FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, true,false);
													fenCanal.ouvrirFenetre();
													fermerFenetre();
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}
									}
								}
							}
						}
					});
					btnRejoinde2.setFont(new Font("Liberation Serif", Font.BOLD, 15));
					btnRejoinde2.setBounds(313, 183, 101, 25);
					panel_1.add(btnRejoinde2);
					
					JButton btnAnnuler2 = new JButton("Annuler");
					btnAnnuler2.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							/* on ferme la internaFrame et on réaffiche les boutons */
							intFenRejCanal.setVisible(false);
							listCanauxModele.removeAllElements(); /* on vide la liste */
							btnRejoindre.setVisible(true);
							btnCompte.setVisible(true);
							btnCreer.setVisible(true);
							if(utilisateur.getGrade()==2){
								btnAdmin.setVisible(true);
							}
						}
					});
					btnAnnuler2.setFont(new Font("Liberation Serif", Font.BOLD, 15));
					btnAnnuler2.setBounds(200, 183, 101, 25);
					panel_1.add(btnAnnuler2);
					
					/* On affiche ensuite la fenêtre interne */
					intFenRejCanal.setVisible(true);
					
					/* Gérer la partie sélection dans la liste + click que le bouton rejoindre */
				}else{
					/* Le serveur n'a pas attesté la bonne réception de la requête */
					JOptionPane.showMessageDialog(panel, "ERREUR, votre requête n'a pas été traitée par le serveur","Erreur requête non traitée",JOptionPane.ERROR_MESSAGE);
				}
				
				
				
			}
		});
		btnRejoindre.setFont(new Font("Liberation Serif", Font.BOLD, 25));
		btnRejoindre.setBounds(373, 264, 275, 35);
		panel.add(btnRejoindre);
		
		
		btnCreer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FenCreationCanal fenCreCan = new FenCreationCanal(sockConnexion, utilisateur);
				fenCreCan.ouvrirFenetre();
				fermerFenetre();
			}
		});
		btnCreer.setFont(new Font("Liberation Serif", Font.BOLD, 25));
		btnCreer.setBounds(373, 311, 275, 35);
		panel.add(btnCreer);
		
		
		btnCompte.setFont(new Font("Liberation Serif", Font.BOLD, 25));
		btnCompte.setBounds(373, 413, 275, 35);
		panel.add(btnCompte);
		
		
		
		JButton btnDeconnexion = new JButton("Déconnexion");
		btnDeconnexion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FenMenuPrincipal fenMenu = new FenMenuPrincipal();
				fenMenu.ouvrirFenetre();
				/* On ferme la connexion et la fenêtre d'inscription */
				try{
					sockConnexion.close();
				}
				catch(IOException ex){
					ex.printStackTrace();
				}
				fermerFenetre();
			}
		});
		btnDeconnexion.setFont(new Font("Liberation Serif", Font.BOLD, 15));
		btnDeconnexion.setBounds(871, 640, 139, 25);
		panel.add(btnDeconnexion);
		
		JLabel libListeActifs = new JLabel("Liste des canaux actifs :");
		libListeActifs.setFont(new Font("Liberation Serif", Font.BOLD, 17));
		libListeActifs.setBounds(12, 84, 180, 27);
		panel.add(libListeActifs);
		
		JButton btnRejoindreCanal = new JButton("Rejoindre");
		btnRejoindreCanal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* On vérifie que l'index sélectionné est supérieur à 0 */
				if(comboCanauxActifs.getSelectedIndex() >= 0){
					/* on envoie une demande au serveur */
					envoyerMesg("8");
					/* On attend la réponse du serveur */
					if(Integer.parseInt(lireMesg()) == 8){
						/* on envoie l'id canal et l'id utilisateur */
						envoyerMesg(String.valueOf(utilisateur.getId())+"#"+correspondanceIndexId.get(comboCanauxActifs.getSelectedIndex()));
						/* On attend la réponse du serveur 0 : erreur / 1 : ok utilisateur / 2 : ok modérateur */
						int rep = Integer.parseInt(lireMesg());
						System.out.println("rep ="+rep);
						if(rep == 0){
							/* erreur */
							JOptionPane.showMessageDialog(panel, "ERREUR, impossible de rejoindre le canal","Erreur impossible de rejoindre le canal",JOptionPane.ERROR_MESSAGE);
						}else{
							/* Si c'est ok pour une connexion on récup les infos du canal */
							String infosCanal = lireMesg();
							/* si commence par 1 : privé si par 0 : public */
							System.out.println("infos canl = "+infosCanal);
							if(rep == 1){
								/* ok utilisateur */
								if(Integer.parseInt(infosCanal.split("/")[0])==1){
									CanalPrive canal = new CanalPrive(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null,null);
									try {
										FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, false,true);
										fenCanal.ouvrirFenetre();
										fermerFenetre();
									} catch (InterruptedException x) {
										// TODO Auto-generated catch block
										x.printStackTrace();
									}
								}else{
									CanalPublic canal = new CanalPublic(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null);
									try {
										FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, false,false);
										fenCanal.ouvrirFenetre();
										fermerFenetre();
									} catch (InterruptedException x) {
										// TODO Auto-generated catch block
										x.printStackTrace();
									}
								}
								
								
							}else{
								/* ok modérateur */
								System.out.println("On rentre en modo");
								if(Integer.parseInt(infosCanal.split("/")[0])==1){
									CanalPrive canal = new CanalPrive(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null,null);
									try {
										FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, true,true);
										fenCanal.ouvrirFenetre();
										fermerFenetre();
									} catch (InterruptedException x) {
										// TODO Auto-generated catch block
										x.printStackTrace();
									}
								}else{
									CanalPublic canal = new CanalPublic(Integer.parseInt(infosCanal.split("/")[1]), infosCanal.split("/")[2], null);
									try {
										FenCanal fenCanal = new FenCanal(sockConnexion, canal, utilisateur, true,false);
										fenCanal.ouvrirFenetre();
										fermerFenetre();
									} catch (InterruptedException x) {
										// TODO Auto-generated catch block
										x.printStackTrace();
									}
								}
							}
						}
					}
				}else{
					JOptionPane.showMessageDialog(panel, "ERREUR, vous n'avez pas sélectionné de canal dans la liste","Erreur index incorrect",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnRejoindreCanal.setFont(new Font("Liberation Serif", Font.BOLD, 12));
		btnRejoindreCanal.setBounds(193, 89, 86, 21);
		panel.add(btnRejoindreCanal);
		
	}
}
