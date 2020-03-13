/* DRemotePrisoner.java */
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

import java.io.Serializable;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;

/*
 *    Class: DRemotePrisoner  
 * --------------------
 * 
 * 
 */
public abstract class DRemotePrisoner<E> implements Serializable, RemotePositionedAgent<E>{
    private static final long serialVersionUID = 1L;
    public E pos; 
    public String id;

	/*
	* Function: DRemotePrisoner
	* --------------------
	* DRemotePrisoner class constructor
	* 
	*
	* returns: -
	*/          
    public DRemotePrisoner(){}

	/*
	* Function: DRemotePrisoner
	* --------------------
	* DRemotePrisoner class constructor
	* 
	* state:
	* 
	* returns: -
	*/       
    public DRemotePrisoner(DistributedState<E> state){
		int i=state.nextId();
		this.id=state.getType().toString()+"-"+i;	
	}

	/*
	* Function: getPos
	* --------------------
	* 
	* 
	* returns: -
	*/           
    @Override
    public E getPos() { return pos; }
    
    /*
	* Function: getId
	* --------------------
	* 
	* 
	* returns: -
	*/           
    @Override
    public String getId() { return id; }

	/*
	* Function: setPos
	* --------------------
	* 
	* pos:
	* 
	* returns: -
	*/         
    @Override
    public void setPos(E pos) { this.pos = pos; }

	/*
	* Function: setId
	* --------------------
	* 
	* id:
	* 
	* returns: -
	*/     
    @Override
    public void setId(String id) {this.id = id;}
    
}
 
