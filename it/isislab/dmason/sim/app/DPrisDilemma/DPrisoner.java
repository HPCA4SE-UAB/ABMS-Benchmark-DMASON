/* DPrisioner.java */
/* 
 * Benchmark model for DMASON ABMS
 * This file is part of the ABMS-Benchmark-DMASON distribution (https://github.com/HPCA4SE-UAB/ABMS-Benchmark-DMASON.git).
 * Copyright (c) 2020 Universitat Autònoma de Barcelona, Escola Universitària Salesiana de Sarrià
 *
 * Based on: Alban Rousset, Bénédicte Herrmann, Christophe Lang, Laurent Philippe
 * A survey on parallel and distributed multi-agent systems for high performance comput-
 * ing simulations Computer Science Review 22 (2016) 27–46 
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License 
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package it.isislab.dmason.sim.app.DPrisDilemma;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.exception.DMasonException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException; 
import java.util.StringTokenizer;

import java.util.Iterator;
import org.jtransforms.fft.DoubleFFT_1D;

import sim.engine.SimState;
import sim.util.*;
import sim.engine.*; 

import java.util.Random;
import java.util.HashSet;

/*
 *    Class: DPrisoner  
 * --------------------
 * 
 * 
 */
public class DPrisoner extends DRemotePrisoner<Double2D> {
    public static final int MAX_AGENTS_TO_PLAY = 10;

    public static final Double2D CENTER_BIRTH = new Double2D(150,150);  
    public static final Double2D CENTER_DEATH = new Double2D(50,50); 
    public static final double BIRTH_RATE = 0.02f; 
    public static final double DEATH_RATE = 0.02f;

    public static final int COMM_BUFF_SIZE = 256;
    
    char[] comm_buffer = new char[COMM_BUFF_SIZE];
    
    public double c, total;
    public long timeCompute, timePlay;

	public Random rnd;

	public double[] fft_input;    

	/*
	* Function: DPrisoner
	* --------------------
	* DPrisoner class constructor
	* 
	*
	* returns: -
	*/      
    public DPrisoner(){
		//System.out.println("DPrisoner()");		
		}

	/*
	* Function: DPrisoner
	* --------------------
	* DPrisoner class constructor
	* 
	* st:
	* 
	* returns: -
	*/   
    public DPrisoner(DistributedState<Double2D> st, Random _rnd){
        super(st);    
 
	final DPrisDilemma mod = (DPrisDilemma) st;
	
	if (mod.fft_input_dprisoner == null){
		fft_input = new double[mod.fft_input.length]; // Fem còpia perquè sinó el Garbage collector l'esborra de DPrisDilemma quan acaba DPrisDilemma.start()?
		// Tindria sentit perquè en aquest constructor es podria accedir perfectament a la info de mod.fft_input, però no a step() o compute()
		System.arraycopy(mod.fft_input, 0, fft_input, 0, mod.fft_input.length);
		mod.fft_input_dprisoner = fft_input;
	}else{
		fft_input = mod.fft_input_dprisoner;
	}
    
        for (int i=0; i<COMM_BUFF_SIZE; i++){
            comm_buffer[i] = (char)i;
		}
      
        this.c = 100;
        this.total = 200;
        this.timePlay = 0;
        this.timeCompute = 0;
	
	this.rnd = _rnd;
    }

	/*
	* Function: step
	* --------------------
	* 
	* 
	* state:
	* 
	* returns: -
	*/       
     public void step(SimState state){
	long timeCompute_tmp = 0;
        final DPrisDilemma mod = (DPrisDilemma) state; 

        Double2D current_position = mod.field.getObjectLocation(this);
        Double2D newloc = move(mod, current_position);

//Detectar agents que han canviat de slot
if (this.getId().equals("0-0-69")){System.out.println("(1) "+mod.TYPE+" "+this.getId()+" "+this.getPos()+" "+(int)comm_buffer[0]+" "+(int)comm_buffer[1]+" c "+this.c );}



//if (this.getId().equals("0-1-1130")){System.out.println("(2)--- "+mod.TYPE+" "+this.getId()+" "+this.getPos());}
//if (this.getId().equals("0-1-1130")){System.out.println("(2)  - "+(int)comm_buffer[0]);}

//if (this.getId().equals("0-0-37")){System.out.println("Step 0-0-37");} 

        play(mod, newloc);

	//timeCompute_tmp = System.currentTimeMillis();
	timeCompute_tmp = System.nanoTime();
        compute();
	//timeCompute += System.currentTimeMillis() - timeCompute_tmp;
	timeCompute += System.nanoTime() - timeCompute_tmp;
      
        if(reproduction(newloc, mod)){
            DPrisoner ag = new DPrisoner(mod, rnd);           
            try {
                ag.setPos(newloc);
                mod.field.setDistributedObjectLocation(ag.pos,ag,state);
               
            } catch (DMasonException e) {
                e.printStackTrace();
            }
        }
         
        if(!die(newloc, mod)){
            //MOVE 
             try {
                mod.field.setDistributedObjectLocation(newloc,this,state);
				//Per fer siguiment d'un agent                
				//if (this.getId().equals("0-0-23")) System.out.println("--- "+mod.TYPE+" "+this.getId()+" "+this.getPos()+" "+mod.field.getObjectLocation(this));            
                
            } catch (DMasonException e) {
                e.printStackTrace();
            }	
        }else{
		//	System.out.println("Defunció "+mod.TYPE+" "+this.getId()+" "+this.getPos()); 	
            mod.killAgent(this); 
        }
    }
    
	/*
	* Function: move
	* --------------------
	* 
	* 
	* mod:
	* current_pos:
	* 
	* returns: -
	*/       
    public Double2D move(DPrisDilemma mod, Double2D current_pos ){
        //double dx = mod.random.nextDouble() < 0.5 ? -1 : 1;
        //double dy = mod.random.nextDouble() < 0.5 ? -1 : 1;
        double dx = rnd.nextDouble() < 0.5 ? -1 : 1;
        double dy = rnd.nextDouble() < 0.5 ? -1 : 1;

        return new Double2D(mod.field.stx(current_pos.x + dx), mod.field.sty(current_pos.y + dy));
    }
    
	/*
	* Function: cooperate
	* --------------------
	* 
	* 
	* mod:
	* 
	* returns: -
	*/         
    public boolean cooperate(DPrisDilemma mod){

         //return mod.random.nextDouble() < c/total;     
         return rnd.nextDouble() < c/total;     
    }

	/*
	* Function: play
	* --------------------
	* 
	* 
	* st:
	* pos:
	* 
	* returns: -
	*/         
    public void play(DPrisDilemma st, Double2D pos){
        Bag neighbours = new Bag();

        int i = 0;
	double separation_distance = 0;
        
        //neighbours = st.field.getNeighborsWithinDistance(pos, st.INTERACTION_RAD, true);
        neighbours = st.field.getNeighborsWithinDistance(pos, st.INTERACTION_RAD, true, false); //boolean nonPointObjects = false
	//neighbours = st.field.getNeighborsExactlyWithinDistance(pos, st.INTERACTION_RAD, true);
	
	//To filter repeated agents at neighbours
	HashSet<String> setAlreadyPlayed = new HashSet<String>();

        Iterator<DPrisoner> it = neighbours.iterator();
        
        double cPayoff = 0;
        double totalPayoff = 0;
        while(it.hasNext()){
            	DPrisoner agentToPlay = it.next();
            	
		if (agentToPlay.getId() == this.getId())   continue;

	    	if (setAlreadyPlayed.contains(agentToPlay.getId())) continue; //To filter repeated agents
		setAlreadyPlayed.add(agentToPlay.getId());
   
		//Note: getNeighborsWithinDistance method for efficiency 
		//      includes also extra agents which are not necessary in the specific range

		separation_distance = euclideanDistance(pos, agentToPlay.getPos());

		//if (this.getId().equals("0-0-69")){System.out.println("Agent "+this.getId()+" juga amb "+agentToPlay.getId()+" "+ pos +" "+agentToPlay.getPos() + " distance " + separation_distance + " neighbours.size() " + neighbours.size() + " i " + i);}

		if (separation_distance <= st.INTERACTION_RAD){
            
            		boolean iCooperated = this.cooperate(st);
			boolean otherCooperated = agentToPlay.cooperate(st); 
            		double payoff = (iCooperated ? ( otherCooperated ? 7 : 1 ) : ( otherCooperated ? 10 : 3));
            
            		if (iCooperated) cPayoff += payoff; 
            		totalPayoff += payoff; 

			//System.out.println("Agent "+this.getId()+" juga amb "+agentToPlay.getId()+" "+this.getPos()+" "+agentToPlay.getPos());   

			//if (this.getId().equals("0-0-69")){System.out.println("Agent "+this.getId()+" juga amb "+agentToPlay.getId()+" "+ pos +" "+agentToPlay.getPos() + " distance " + separation_distance);}
            
			//if (this.getId().equals("0-0-69")){System.out.println("Step 0-0-69 " + st.TYPE + " i " + i + " iCooperated " +  iCooperated +  " payoff "+  payoff +" cPayoff " +  cPayoff + " totalPayoff "  + totalPayoff + " neighbours.size() " + neighbours.size());} 

            		i++; 
            		if (i >= MAX_AGENTS_TO_PLAY) break;     // Controls max number of agents to play with
		}

        }
 
        this.c += cPayoff;
        this.total += totalPayoff;
	//if (this.getId().equals("0-0-26")){System.out.println("-Step 0-0-26 "+ st.TYPE + " i " + i + " c " +  c + " total "  + total);} 
    }

	/*
	* Function: compute
	* --------------------
	* 
	* 
	* 
	* returns: -
	*/            
     public void compute(){		 
        double [] fft_in = new double[fft_input.length];
	double fft_out_sum = 0; 
	System.arraycopy(fft_input, 0, fft_in, 0, fft_input.length); //Es duplica perquè DoubleFFT_1D la utilitza com a retorn

	DoubleFFT_1D fft = new DoubleFFT_1D(fft_in.length/2);
	fft.complexForward(fft_in);  		

	for(int i=0;i<fft_in.length  ;i++) {
		fft_out_sum += fft_in[i];	            
        }

/*if (this.getId().equals("0-0-23")){System.out.println("fft_out_sum  "+fft_out_sum);}

if (this.getId().equals("0-0-23")){
	System.out.println("(DPrisioner.compute) {");
	for(int i=0;i<10;i++) {
            System.out.print(fft_in[i]+" ");
	}
	System.out.println(" ");
}*/
    }
   
	/*
	* Function: reproduction
	* --------------------
	* 
	* 
	* pos:
	* mod:
	* 
	* returns: -
	*/            
     public Boolean reproduction(Double2D pos, DPrisDilemma mod){
        double birth_rate_factor = BIRTH_RATE*(1 - Math.min(1 , Math.sqrt( Math.pow(Math.abs(pos.x-CENTER_BIRTH.x),2) + Math.pow(Math.abs(pos.y-CENTER_BIRTH.y),2) )/((mod.gridHeight+mod.gridWidth)/2)) );
        
        //return ( mod.random.nextDouble() < birth_rate_factor ? true : false);
        return ( rnd.nextDouble() < birth_rate_factor ? true : false);
    }

	/*
	* Function: die
	* --------------------
	* 
	* 
	* pos:
	* mod:
	* 
	* returns: -
	*/               
    public Boolean die (Double2D pos, DPrisDilemma mod){
        double death_rate_factor = DEATH_RATE * (1 - Math.min(1 , Math.sqrt( Math.pow(Math.abs(pos.x-CENTER_DEATH.x),2) + Math.pow(Math.abs(pos.y-CENTER_DEATH.y),2) )/((mod.gridHeight+mod.gridWidth)/2)) );
        
        //Boolean miau = mod.random.nextDouble() < death_rate_factor ? true : false;
        Boolean miau = rnd.nextDouble() < death_rate_factor ? true : false;
        return miau;
    }

        /*
 	*Function: euclideanDistance
 	* --------------------
 	* 
 	* 
 	* me_location:
 	* other_location:
 	* 
 	* returns: -
 	*/
        public static double euclideanDistance(Double2D me_location, Double2D other_location){

                return Math.sqrt(Math.pow((me_location.x - other_location.x),2) + Math.pow((me_location.y - other_location.y),2));
        }



        
    
}
