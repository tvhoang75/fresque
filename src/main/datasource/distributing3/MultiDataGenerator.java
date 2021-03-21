package main.datasource.distributing3;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import base.Constants;
import base.DynamicProperties;

public class MultiDataGenerator {
	public static void main(String[] args) {
		while (true) {
			int numGenerator = Integer.parseInt(DynamicProperties.getInstance().getProperties()
					.getProperty("num.generator"));
			for(int i = 0; i < numGenerator; i++) {
				System.out.println("data generator " + (i+1));
				//create a new thread object of data generator
				Thread t = new DataGenerator();
				t.start();
				try {
					TimeUnit.MILLISECONDS.sleep(Constants.DataGenerator.TIME_GENERATE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Scanner scan= new Scanner(System.in);
				System.out.println("How many generator more ? ");
				numGenerator = scan.nextInt();
				scan.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}
}
