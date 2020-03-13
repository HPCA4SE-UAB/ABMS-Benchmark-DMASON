/* 
 * Generation 0.xml file for benchmark FLAME ABMS model
 * This file is part of the ABMS-Benchmark-FLAME distribution (https://github.com/HPCA4SE-UAB/ABMS-Benchmark-FLAME.git).
 * Copyright (c) 2018 Universitat Autònoma de Barcelona, Escola Universitària Salesiana de Sarrià
 *
 *Based on: Alban Rousset, Bénédicte Herrmann, Christophe Lang, Laurent Philippe
 *A survey on parallel and distributed multi-agent systems for high performance comput-
 *ing simulations Computer Science Review 22 (2016) 27–46
 *
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
  
 *  You should have received a copy of the GNU General Public License 
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


#include <stdio.h>
#include <fstream>
#include <iostream>
#include <ios> 
#include <stdlib.h>  
#include <math.h>
#include <string.h>
#include <uuid/uuid.h>
extern "C"{
#include "fnv.h"
}


using namespace std;


/*
 * Function:  isPowerOfTwo 
 * --------------------
 * Check power of two
 *
 * n: number to check
 *
 * returns: true
 * 	    false
*/
bool isPowerOfTwo(int n) 
{ 
   if(n==0) 
   return false; 
  
   return (ceil(log2(n)) == floor(log2(n))); 
} 


 
/*
 * Function:  main 
 * --------------------
 * Generation 0.xml file for benchmark FLAME ABMS model
 * Execute: ./get_file_init  num_persons birth_rate death_rate
 *
 * num_persons: number of agents
 * birth_rate: birth probability, inteval [0,1], 0: no birth, 1: 100% probability of birth
 * death_rate: death probability, inteval [0,1], 0: no death, 1: 100% probability of death
 *
 * returns: 0
 * 	    0.xml file for FLAME
*/

int main(int argc, char *argv[]) { 
	ofstream objetfichier, objetfichier2, objetfichier3;

	uuid_t uuid;
	Fnv32_t hash_val;

	int fft_vector_size = atof(argv[4]);
	if (!(isPowerOfTwo(fft_vector_size))){
		cout << "argv[4] must be a power of 2" << endl;
		return 0;
	}

	objetfichier.open("0.xml", ios::out); 
	objetfichier2.open("0.data", ios::out); 
	srand (time(NULL));
	int num_persons;
	num_persons = atoi(argv[1]);
	float birth_rate;
	birth_rate = atof(argv[2]);
	float death_rate;
	death_rate = atof(argv[3]);
	
	// creating the structure of the XML 
	objetfichier << "<states>" << "\n";
	objetfichier << "<itno>0</itno>" << "\n";

	objetfichier << "<environment>" << "\n";
	objetfichier << "	<height>300</height>" << "\n";
	objetfichier << "	<width>300</width>" << "\n";
	objetfichier << "	<radius>10</radius>" << "\n";
	objetfichier << "	<birth_rate>"<<birth_rate <<"</birth_rate>" << "\n";
	objetfichier << "	<center_birth_x>"<<150<<"</center_birth_x>" << "\n";
	objetfichier << "	<center_birth_y>"<<150<<"</center_birth_y>" << "\n";
	objetfichier << "	<death_rate>"<< death_rate <<"</death_rate>" << "\n";
	objetfichier << "	<center_death_x>"<<50<<"</center_death_x>" << "\n";
	objetfichier << "	<center_death_y>"<<50<<"</center_death_y>" << "\n";
	objetfichier << "</environment>" << "\n";
	
	objetfichier << "<agents>" << "\n";

	// Creating agent 
	for(int i=1; i<=num_persons; i++) {
		int x = rand()%(299-0);
		int y = rand()%(299-0);
		//int z = rand()%(299-0);
		int z = 0;
		objetfichier << "<xagent>" << "\n";
		objetfichier << "	<name>person</name>" << "\n";
		
		uuid_generate(uuid);

		hash_val = fnv_32_buf(uuid, 16, FNV1_32_INIT);
		objetfichier << "	<id>" << hash_val  <<"</id>" << "\n";
		objetfichier << "	<x>" << x << "</x>" << "\n";
		objetfichier << "	<y>" << y << "</y>" << "\n";
		objetfichier << "       <z>" << z << "</z>" << "\n";
		objetfichier2 << hash_val << " " << x << " " << y << " " << z << "\n";
		objetfichier << "	<c>100</c>" << "\n";
		objetfichier << "	<total>200</total>" << "\n";		
		objetfichier << "</xagent>" << "\n";
	}
	
	objetfichier << "</agents>" << "\n";
	objetfichier << "</states>" << "\n";

	objetfichier.close();  
	objetfichier2.close();  

	//  Creating FFT vector fille
	objetfichier3.open("fft.data", ios::out);


	objetfichier3 << fft_vector_size << "\n";

       for(int i=1; i <= fft_vector_size; i++) {
                 objetfichier3 << ((double)rand()/(RAND_MAX)) << " " << ((double)rand()/(RAND_MAX)) << "\n";
	}

	objetfichier3.close();
	
	return 0; 
} 

