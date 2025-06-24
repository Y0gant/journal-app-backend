package com.y0gant.springDemo.service;

import org.springframework.stereotype.Service;

import java.util.Random;


@Service
public class SentimentService {

    //Supposedly machine learning method
    //Simulates sentiment analysis by randomly generating a sentiment score.
    public int getSentiment(String text) {
        Random rand = new Random();
        return rand.nextInt(11);
    }

}
