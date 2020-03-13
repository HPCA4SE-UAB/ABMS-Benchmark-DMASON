/* DPrisDilemma.java */
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

import it.isislab.dmason.annotation.ReduceAnnotation;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;

import java.io.*; 
import java.util.StringTokenizer;

import sim.util.*;
import sim.engine.*;

import java.util.Iterator;

/*
 *    Class: DPrisDilemma  
 * --------------------
 * 
 * 
 */
public class DPrisDilemma extends DistributedState<Double2D>{
    public DPrisDilemma(){ super(); }
    
    private static final long serialVersionUID = 1L;
	public DContinuousGrid2D field;
    
    public double gridWidth ;
	public double gridHeight ;   
	public int MODE;
    
    public final double INTERACTION_RAD = 10.0;
    public int discretization = 2*(int)INTERACTION_RAD;
    
    public String FFT_FILE = "fft.data";
    public double[] fft_input;
    
    public String AGENTS_FILE = "0_1000.data";
    public int num_agents = 1000;
    
    public long startTime;


	/*
	* Function: DPrisDilemma
	* --------------------
	* DPrisDilemma class constructor
	* 
	* params: 
	* prefix:
	*
	* returns: -
	*/   
	public DPrisDilemma(GeneralParam params,String prefix){    	
		super(params,new DistributedMultiSchedule<Double2D>(),prefix,params.getConnectionType());
		this.topicPrefix=prefix;
		this.MODE=params.getMode();
		this.gridWidth=params.getWidth();
		this.gridHeight=params.getHeight();
      
	}

	/*
	* Function: DPrisDilemma
	* --------------------
	* DPrisDilemma class constructor
	* 
	* params: 
	* simParams:
	* prefix
	*
	* returns: -
	*/   	
	public DPrisDilemma(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(), prefix,params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
		topicPrefix = prefix; 
        
		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {

				e.printStackTrace();
			} catch (SecurityException e) {

				e.printStackTrace();
			} catch (IllegalAccessException e) {

				e.printStackTrace();
			} catch (NoSuchFieldException e) {

				e.printStackTrace();
			}
		}

		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}

		}

	}

	/*
	* Function: start
	* --------------------
	* 
	* 
	* returns: -
	*/   	
    
    @Override
	public void start(){
        super.start();
        
        startTime = System.currentTimeMillis();     
        
		try {
			field = DContinuousGrid2DFactory.createDContinuous2D(discretization,gridWidth, gridHeight,this,
					super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"prisoners", topicPrefix,true);
			init_connection();
		} catch (DMasonException e) { e.printStackTrace(); }
        
        try (FileReader reader = new FileReader(FFT_FILE)){
            BufferedReader br = new BufferedReader(reader);
            int i = 0; 

            // Parte real = 2*i | parte imaginaria 2*i+1
            fft_input = new double[2*Integer.parseInt(br.readLine())];

            String line; 
            while((line = br.readLine()) != null){
                StringTokenizer tk = new StringTokenizer(line);

                //REAL
                fft_input[i] = Double.parseDouble(tk.nextToken());
                                
                //IMAGINARIA
                fft_input[i+1] = Double.parseDouble(tk.nextToken());
                
                i+=2;
            }
            System.out.println("FFT SIZE : "+ fft_input.length);
        }catch(IOException e){
            System.err.format("IOException %s%n", e);
        }
        
        // Aqui se crean los agentes 
        //Creas = num agentes x reg 
        /*int agentsToCreate=0;   //Agentes que tiene que crear esta region 
		int remainder=super.NUMAGENTS%super.NUMPEERS; 

		if(remainder==0)
			agentsToCreate= super.NUMAGENTS / super.NUMPEERS;
		else if(remainder!=0 && TYPE.pos_i==0 && TYPE.pos_j==0)
			agentsToCreate= (super.NUMAGENTS / super.NUMPEERS)+remainder;
        else
			agentsToCreate= super.NUMAGENTS / super.NUMPEERS;
		*/

        Double2D[] agent_position = loadAgentPositions();
        int i = 0; 
       
        DPrisoner prisoner = new DPrisoner(this); 

        //while(field.size() != agentsToCreate  && i < agent_position.length-1/**/){
        while(i < agent_position.length){
			
            if (field.myfield.isMine(agent_position[i].x,agent_position[i].y) || 
            field.rmap.EAST_MINE.isMine(agent_position[i].x,agent_position[i].y)  || 
            field.rmap.WEST_MINE.isMine(agent_position[i].x,agent_position[i].y) ||
            field.rmap.SOUTH_MINE.isMine(agent_position[i].x,agent_position[i].y) ||
            field.rmap.NORTH_MINE.isMine(agent_position[i].x,agent_position[i].y)){
                
                //System.out.println("AGENT "+ agent_position[i] +" belongs to this region " + "(" + prisoner.getId() + ")");
                prisoner.setPos(agent_position[i]);

                field.setObjectLocation(prisoner,prisoner.pos);
                schedule.scheduleOnce(prisoner);

                if(field.size() != super.NUMAGENTS)
                    prisoner = new DPrisoner(this);
                                     
            }
            i++;
        }    
        
        System.out.println("Agentes cargados de fichero con éxito ! ___ "+field.size());
        long stopTime = System.currentTimeMillis();
        
    }

	/*
	* Function: main
	* --------------------
	* main function
	* 
	* args: 
	*
	* returns: -
	*/   
    public static void main(String[] args){

		doLoop(DPrisDilemma.class, args);
		System.exit(0);
	}
 
 
 	/*
	* Function: loadAgentPositions
	* --------------------
	* 
	* 
	*
	* returns: -
	*/      
    public Double2D[] loadAgentPositions(){
        // CAMBIAR ESTO Y PILLARLO DEL NOMBRE DEL FICHERO.
        Double2D[] agent_pos = new Double2D[num_agents]; 
        System.out.println("LOADING AGENT * POSITIONS ... "+AGENTS_FILE);
        try (FileReader reader = new FileReader(AGENTS_FILE)){
            BufferedReader br = new BufferedReader(reader);
            int i=0;
            
            String line; 
            while((line = br.readLine()) != null){

                //System.out.println(line);
                StringTokenizer tk = new StringTokenizer(line);
                
                //System.out.println(line);
                //No usamos el 1o = ID agente (?)
                tk.nextToken();
                
                double x = Double.parseDouble(tk.nextToken()); 
                double y = Double.parseDouble(tk.nextToken());        
                
                agent_pos[i] = new Double2D(x,y);
                i++;
            }
            br.close();
                        
        }catch(IOException e){
           System.err.format("IOException ", e);
        }
       
        return agent_pos;
    }

 	/*
	* Function: finish
	* --------------------
	* 
	* 
	*
	* returns: -
	*/        
    @Override
    public void finish(){
        System.out.println("FINISH - MODEL");
        end_sim_prints();
        kill();     
    }

 	/*
	* Function: end_sim_prints
	* --------------------
	* 
	* 
	*
	* returns: -
	*/   
    public void end_sim_prints(){
        Bag agents = new Bag();
        agents = field.getAllObjects();
        
        Iterator<DPrisoner> it = agents.iterator();
        System.out.println("WRITE SIZE IT : "+ agents.size());
        
        try {
            System.out.println("Writting... ");
            writePositions(it,agents.size());
            
            it = agents.iterator();
            //System.out.println("PAYOFF SIZE IT : "+ iterator.size(it));
            
            printPayoffs(it, agents.size());
        }catch(IOException ioe){
            System.out.println("Error at end_sim_prints");
        }
        System.out.println("NUM AGENTS : "+ agents.size()+ " || "+field.size());
        long TotalTime = System.currentTimeMillis() - startTime;
        System.out.println("["+field.own_x+" , "+field.own_y+"] Total time : "+TotalTime);

    }

 	/*
	* Function: writePositions
	* --------------------
	* 
	* 
	* it: num_agents
	* 
	*
	* returns: -
	*/       
    public void writePositions(Iterator<DPrisoner> it, int num_agents) throws   UnsupportedEncodingException,FileNotFoundException {
        String pos_file = "logs/results/"+TYPE+"_"+num_agents+"_positions.txt";
        PrintWriter writer = new PrintWriter(pos_file, "UTF-8");

        while(it.hasNext()){
            DPrisoner agentInField = it.next();
            Double2D pos = agentInField.getPos();
            //System.out.println(TYPE+" -- "+agentInField.getId());
            
            if (field.myfield.isMine(pos.x,pos.y) || 
            field.rmap.EAST_MINE.isMine(pos.x,pos.y)  || 
            field.rmap.WEST_MINE.isMine(pos.x,pos.y) ||
            field.rmap.SOUTH_MINE.isMine(pos.x,pos.y) ||
            field.rmap.NORTH_MINE.isMine(pos.x,pos.y)){
                writer.println(pos.x+" "+pos.y);
                writer.println(agentInField.getId()+" "+agentInField.getPos());

            }

        }      

        writer.close();
        
    }
    
 	/*
	* Function: printPayoffs
	* --------------------
	* 
	* 
	* it: 
	* num_agents:
	* 
	*
	* returns: -
	*/      
    public void printPayoffs(Iterator<DPrisoner> it, int num_agents)  {
        //hacer medias payoffs
        //System.out.println("PAYOFFS : ");
        double cPayoffMean = 0, totalPayoffMean = 0;
        long compTime = 0, playTime = 0;
        
        while(it.hasNext()){
            DPrisoner agentInField = it.next();
            
            cPayoffMean += agentInField.c;
            totalPayoffMean += agentInField.total;
            
            compTime += agentInField.timeCompute;    
            playTime += agentInField.timePlay;    
            //System.out.println("IN PROGRESS -- c : "+agentInField.getC()+" -- total : "+agentInField.getTotal());
        }
        
        cPayoffMean = cPayoffMean/num_agents;        
        totalPayoffMean = totalPayoffMean/num_agents;
        
        compTime = compTime; 
        playTime = playTime; 
        
        System.out.println("["+field.own_x+" , "+field.own_y+"] totalPayoff mean : "+totalPayoffMean+" -- cPayoff mean : "+cPayoffMean);
        System.out.println("["+field.own_x+" , "+field.own_y+"] Compute time : "+compTime+" -- Play time : "+playTime);
    }

 	/*
	* Function: DPrisoner
	* --------------------
	* 
	* 
	* agent: 
	* 
	*
	* returns: -
	*/    
    protected void killAgent(DPrisoner agent){
		//System.out.println("["+field.size()+"]"+" Removing agent "+agent.getId());
        field.remove(agent);
        //System.out.println("["+field.size()+"]"+" Removed "+agent.getId());
	}
    
 	/*
	* Function: getField
	* --------------------
	* 
	* 
	* agent: 
	* 
	*
	* returns: -
	*/       
	@Override
	public DistributedField2D getField() {
		return field;
	}

 	/*
	* Function: addToField
	* --------------------
	* 
	* 
	* rm: 
	* loc:
	* 
	*
	* returns: -
	*/   
	@Override
	public void addToField(RemotePositionedAgent rm, Double2D loc) {
        //System.out.println("["+rm.getId()+"] Cambia region de "+rm.getPos()+" a : "+loc );
		field.setObjectLocation(rm,loc);
	}
 
 	/*
	* Function: getState
	* --------------------
	* 
	* 
	* agent: 
	* 
	*
	* returns: -
	*/   
	@Override
	public SimState getState() {
		return this;
	}

 	/*
	* Function: setPortrayalForObject
	* --------------------
	* 
	* 
	* o: 
	* 
	*
	* returns: -
	*/   
	public boolean setPortrayalForObject(Object o) {
		return false;
	}    
    
}
