package com.anand.crypto.ecc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

import com.anand.crypto.ecc.EllipticCurve;
import com.anand.crypto.ecc.Point;
public class Driver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EllipticCurve ec = null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			ec = handleEllipticCurveInput(bufferedReader);
			boolean flag = true;
			while(flag){
				System.out.println("> what do you want to do? (s:low hops, "
						+ "f:ast hops with double & add, c:hange curve, e:xit)");
				try {
					String command = bufferedReader.readLine();
					switch(command) {
						case "s":
							handlePointHoppingInput("slow", bufferedReader, ec);
							break;
						case "f":
							handlePointHoppingInput("fast", bufferedReader, ec);
							break;
						case "c":
							flag = false;
							break;
						case "e":
							System.exit(0);
							break;
					}
				} catch (Exception e) { System.out.println("invalid input."); }
			}
		}

	}
	static EllipticCurve handleEllipticCurveInput(BufferedReader bufferedReader) {
		EllipticCurve ec = null;
		boolean flag = true;
		while (flag) {
			try { 
				boolean inputFlag = true;
				while (inputFlag) {
					System.out.println("> please enter a, b, & p (E: y^2 <congruent> x^3 + ax + b mod p), or e:xit");
					String[] input = bufferedReader.readLine().split(" ");
					if (input[0].equalsIgnoreCase("e")) System.exit(0);
					BigInteger a = new BigInteger(input[0]); BigInteger b = 
							new BigInteger(input[1]); BigInteger p = new BigInteger(input[2]);
					if (EllipticCurve.isNonsingular(a, b)) {
						ec = new EllipticCurve(a, b, p);
						System.out.println();
						inputFlag = false;
					} else System.out.println("invalid entry: singular elliptic curve");
				}
				inputFlag = true;
				while (inputFlag) {
					try {
						if (handleGeneratorPointInput(ec, bufferedReader)) { inputFlag = false;}
						else { System.out.println("Point not on curve."); }
					} catch (Exception e) { System.out.println("invalid input."); }
				}
				flag = false;
			} catch (Exception e) {
				System.out.println("invalid input (usage: a b p where E: y^2 <congruent> x^3 + ax + b mod p)");
				
			}
		}
		return ec;
	}
	static boolean handleGeneratorPointInput(EllipticCurve ec, BufferedReader bufferedReader) throws IOException {
		System.out.println("> please enter Generator Point x & y coordinates, or e:xit");
		String[] input = bufferedReader.readLine().split(" ");
		if (input[0].equalsIgnoreCase("e")) System.exit(0);
		Point G = new Point(new BigInteger(input[0]), new BigInteger(input[1]));
		return ec.update(G);		
	}
	static void handlePointHoppingInput(String type, BufferedReader bufferedReader, EllipticCurve ec) throws IOException {
		System.out.println("> please enter # of point hops d");
		String[] input = bufferedReader.readLine().split(" ");
		BigInteger d = new BigInteger(input[0]);
		if(type.equals("slow")) ec.slowMultiply(d);
		else if(type.equals("fast")) {
			if(d.compareTo(ec.getN())<=0) ec.fastMultiply(d);
			else System.out.println("# of Point hops d must be <= "+ec.getN()
				+" (i.e. n, the # of points in cyclic subgroup)");
		}
	}
}

	


