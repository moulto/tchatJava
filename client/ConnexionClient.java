/**
 * 
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.InetAddress;

/**
 * @author florian
 *
 */
public class ConnexionClient {
	
	/**
	 * Socket permettant de réaliser la connexion au serveur
	 */
	Socket sockConnexion = null;
	
	/**
	 * Client qui souhaite se connecter
	 */
	Utilisateur client = null;
	
	/**
	 * Buffer permettant de lire les messages du serveur
	 */
	BufferedReader lire = null;
	
	/**
	 * Message reçu du serveur
	 */
	String message = null;
	
	/**
	 * Constructeur d'intialiser la connexion entre le client et le serveur.
	 * @param client Identité du client demandant la connexion (utilisé pour les clients sur liste noire)
	 */
	public ConnexionClient(Utilisateur client) {
		this.client = client;
	}
	
	/**
	 * Réalise la connexion du client au serveur et intialise la socket de connexion.
	 */
	public void connect(){
		/* On tente de connecter le client au serveur en lui envoyant l'identifiant du client */
		try{
			this.sockConnexion = new Socket(InetAddress.getLocalHost(), 2369);
			/* On initialise le buffer de lecture pour récupérer la confirmation de connexion de la part du serveur */
			lire = new BufferedReader(new InputStreamReader(sockConnexion.getInputStream()));
			/* On récupère le message du serveur */
			message = lire.readLine();
			/* On l'affiche */
			System.out.println("Mesage du serveur : "+message);
			
		}
		catch(IOException exception){
			System.out.println("ERREUR : Connexion impossible au serveur");
		}
		
	}

}
