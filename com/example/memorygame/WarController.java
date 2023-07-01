package com.example.memorygame;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class WarController /*implements Initializable*/ {

    //@FXML
    public ImageView player1ImageView;

    //@FXML
    public Label player1Label;

    //@FXML
    public ImageView player2ImageView;

    //@FXML
    public Label player2Label;

    //@FXML
    public Label warDeckLabel;

    private ArrayList<Card> player1;
    private ArrayList<Card> player2;
    private ArrayList<Card> warDeck;

    //@FXML
    void playNextHand() {
        player1ImageView.setVisible(true);
        player2ImageView.setVisible(true);

        //while both players have at least 1 card
        if (player1.size()>0 && player2.size()>0)
        {
            Card card1 = player1.remove(0);
            Card card2 = player2.remove(0);
            warDeck.add(card1);
            warDeck.add(card2);
            //System.out.printf("Card 1: %s  Card 2: %s  warDeck: %s%n",card1,card2,warDeck);

            player1ImageView.setImage(card1.getImage());
            player2ImageView.setImage(card2.getImage());

            //player 1 winds
            if (card1.getValue()> card2.getValue())
                player1.addAll(warDeck);
            else if (card2.getValue()>card1.getValue())
                player2.addAll(warDeck);
            else
                playWar();

            updateLabels();
        }
        else
        {
            //find winner
        }
        warDeck.clear();
    }

    //@Override
    public void initialize(String location, String resources) {
        player1 = new ArrayList<>();
        player2 = new ArrayList<>();
        warDeck = new ArrayList<>();

        DeckOfCards deck = new DeckOfCards();
        deck.shuffle();

        for(int i=1;i<=26;i++)
        {
            player1.add(deck.dealTopCard());
            player2.add(deck.dealTopCard());
        }

        //for testing a war - remove
//        Card aceOfSpades = new Card("spades","ace");
//        player1.add(0, aceOfSpades);
//        player2.add(0, aceOfSpades);

        player1ImageView.setVisible(false);
        player2ImageView.setVisible(false);
    }

    private void updateLabels()
    {
        player1Label.setText(player1.size() + " Cards");
        player2Label.setText(player2.size() + " Cards");
        warDeckLabel.setText(warDeck.size() + " Cards");
    }

    private void playWar()
    {
        if (player1.size()<4)
        {
            //player 2 wins
        }
        else if (player2.size()<4)
        {
            //player1 wins
        }
        else
        {
            //both players add 3 cards to the war deck and flip their top card
            for (int i=1; i<=3; i++)
            {
                warDeck.add(player1.remove(0));
                warDeck.add(player2.remove(0));
            }
            playNextHand();
        }
    }
}
