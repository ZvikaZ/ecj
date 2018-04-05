/*
  Portions copyright 2010 by Sean Luke, Robert Hubley, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.spea2;

import ec.*;
import ec.util.*;
import ec.multiobjective.*;
import ec.simple.*;
import java.util.*;

/* 
 * SPEA2Breeder.java
 * 
 * Created: Sat Oct 16 11:24:43 EDT 2010
 * By: Faisal Abidi and Sean Luke
 * Replaces earlier class by: Robert Hubley, with revisions by Gabriel Balan and Keith Sullivan
 */

/**
 * This subclass of SimpleBreeder overrides the loadElites method to build an archive in the top elites[subpopnum]
 * of each subpopulation.  It computes the sparsity metric, then constructs the archive.
 */

public class SPEA2Breeder extends SimpleBreeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
                        
        if (sequentialBreeding) // uh oh, haven't tested with this
            state.output.fatal("SPEA2Breeder does not support sequential evaluation.",
                base.push(P_SEQUENTIAL_BREEDING));

        if (!clonePipelineAndPopulation)
            state.output.fatal("clonePipelineAndPopulation must be true for SPEA2Breeder.");
        }


    protected void loadElites(EvolutionState state, Population newpop)
        {
        // are our elites small enough?
        for(int x = 0; x< state.population.subpops.size(); x++)
            if (numElites(state, x)> state.population.subpops.get(x).individuals.size())
                state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation");
        state.output.exitIfErrors();

        // do it
        for (int sub = 0; sub < state.population.subpops.size(); sub++)
            {
            ArrayList<Individual> newInds = newpop.subpops.get(sub).individuals;  // The new population after we are done picking the elites
            ArrayList<Individual> oldInds = state.population.subpops.get(sub).individuals;   // The old population from which to pick elites
                        
            computeAuxiliaryData(state, oldInds);
            buildArchive(state, oldInds, newInds, numElites(state, sub));
            }

        // optionally force reevaluation
        unmarkElitesEvaluated(state, newpop);
        }

    public double[] calculateDistancesFromIndividual(Individual ind, ArrayList<Individual> inds)
        {
        double[] d = new double[inds.size()];
        for(int i = 0; i < inds.size(); i++)
            d[i] = ((SPEA2MultiObjectiveFitness)ind.fitness).sumSquaredObjectiveDistance((SPEA2MultiObjectiveFitness)inds.get(i).fitness);
        // now sort
        Arrays.sort(d);
        return d;
        }


    public void buildArchive(EvolutionState state, ArrayList<Individual> oldInds, ArrayList<Individual> newInds, int archiveSize)
        {                
        // step 1: load the archive with the pareto-nondominated front
        ArrayList<Individual> archive = new ArrayList<Individual>();
        ArrayList<Individual> nonFront = new ArrayList<Individual>();
        MultiObjectiveFitness.partitionIntoParetoFront(oldInds, archive, nonFront);
                
        // step 2: if the archive isn't full, load the remainder with the fittest individuals (using customFitnessMetric) that aren't in the archive yet
        if (archive.size() < archiveSize)
            {
            Collections.sort(nonFront);  // the fitter individuals will be earlier
            int len = (archiveSize - archive.size());
            for(int i = 0; i < len; i++)
                {
                archive.add(nonFront.get(i));
                }
            }
                        

        // step 3: if the archive is OVERFULL, iterate as follows:
        //              step 3a: remove the k-closest individual in the archive

        //SPEA2Evaluator evaluator = ((SPEA2Evaluator)(state.evaluator));
        // Individual[] inds = (Individual[])(archive.toArray(dummy));
                
        while(archive.size() > archiveSize)
            {
            Individual closest = (Individual)(archive.get(0));
            int closestIndex = 0;
            double[] closestD = calculateDistancesFromIndividual(closest, oldInds);
                        
            for(int i = 1; i < archive.size(); i++)
                {
                Individual competitor = (Individual)(archive.get(i));
                double[] competitorD = calculateDistancesFromIndividual(competitor, oldInds);
                                
                for(int k = 0; k < oldInds.size(); k++)
                    {
                    if (closestD[i] > competitorD[i])
                        { closest = competitor ; closestD = competitorD;  closestIndex = k; break; }
                    else if (closestD[i] < competitorD[i])
                        { break; }
                    }
                }
                        
            // remove him destructively -- put the top guy in his place and remove the top guy.  This is O(1)
            archive.set(closestIndex, archive.get(archive.size()-1));
            archive.remove(archive.size()-1);
            }
                                                
        // step 4: put archive into the new individuals
        newInds.addAll(archive);
        }


    /** Computes the strength of individuals, then the raw fitness (wimpiness) and kth-closest sparsity
        measure.  Finally, computes the final fitness of the individuals.  */
    public void computeAuxiliaryData(EvolutionState state, ArrayList<Individual> inds)
        {
        double[][] distances = calculateDistances(state, inds);
                        
        // For each individual calculate the strength
        for(int y=0;y<inds.size();y++)
            {
            // Calculate the node strengths
            int myStrength = 0;
            for(int z=0;z<inds.size();z++)
                if (((SPEA2MultiObjectiveFitness)inds.get(y).fitness).paretoDominates((MultiObjectiveFitness)inds.get(z).fitness)) 
                    myStrength++;
            ((SPEA2MultiObjectiveFitness)inds.get(y).fitness).strength = myStrength;
            } //For each individual y calculate the strength
                
        // calculate k value
        int kTH = (int) Math.sqrt(inds.size());  // note that the first element is k=1, not k=0 
        
        // For each individual calculate the Raw fitness and kth-distance
        for(int y=0;y<inds.size();y++)
            {
            double fitness = 0;
            for(int z=0;z<inds.size();z++)
                {
                // Raw fitness 
                if ( ((SPEA2MultiObjectiveFitness)inds.get(z).fitness).paretoDominates((MultiObjectiveFitness)inds.get(y).fitness) )
                    {
                    fitness += ((SPEA2MultiObjectiveFitness)inds.get(z).fitness).strength;
                    }
                } // For each individual z calculate RAW fitness distances
            // Set SPEA2 raw fitness value for each individual
                                    
            SPEA2MultiObjectiveFitness indYFitness = ((SPEA2MultiObjectiveFitness)inds.get(y).fitness);
                        
            // Density component
                        
            // calc k-th nearest neighbor distance.
            // distances are squared, so we need to take the square root.
            double kthDistance = Math.sqrt(orderStatistics(distances[y], kTH, state.random[0]));
                        
            // Set SPEA2 k-th NN distance value for each individual
            indYFitness.kthNNDistance = 1.0 / ( 2 + kthDistance);
                        
            // Set SPEA2 fitness value for each individual
            indYFitness.fitness = fitness + indYFitness.kthNNDistance;
            }
        }
    
        
    /** Returns a matrix of sum squared distances from each individual to each other individual. */
    public double[][] calculateDistances(EvolutionState state, ArrayList<Individual> inds)
        {
        double[][] distances = new double[inds.size()][inds.size()];
        for(int y=0;y<inds.size();y++)
            {
            distances[y][y] = 0;
            for(int z=y+1;z<inds.size();z++)
                {
                distances[z][y] = distances[y][z] =
                    ((SPEA2MultiObjectiveFitness)inds.get(y).fitness).
                    sumSquaredObjectiveDistance( (SPEA2MultiObjectiveFitness)inds.get(z).fitness );
                }
            }
        return distances;
        }


    /** Returns the kth smallest element in the array.  Note that here k=1 means the smallest element in the array (not k=0).
        Uses a randomized sorting technique, hence the need for the random number generator. */
    double orderStatistics(double[] array, int kth, MersenneTwisterFast rng)
        {
        return randomizedSelect(array, 0, array.length-1, kth, rng);
        }
                
                
    /* OrderStatistics [Cormen, p187]:
     * find the ith smallest element of the array between indices p and r */
    double randomizedSelect(double[] array, int p, int r, int i, MersenneTwisterFast rng)
        {
        if(p==r) return array[p];
        int q = randomizedPartition(array, p, r, rng);
        int k = q-p+1;
        if(i<=k)
            return randomizedSelect(array, p, q, i,rng);
        else
            return randomizedSelect(array, q+1, r, i-k,rng);
        }
                
                
    /* [Cormen, p162] */
    int randomizedPartition(double[] array, int p, int r, MersenneTwisterFast rng)
        {
        int i = rng.nextInt(r-p+1)+p;
                
        //exchange array[p]<->array[i]
        double tmp = array[i];
        array[i]=array[p];
        array[p]=tmp;
        return partition(array,p,r);
        }
                
                
    /* [cormen p 154] */
    int partition(double[] array, int p, int r)
        {
        double x = array[p];
        int i = p-1;
        int j = r+1;
        while(true)
            {
            do j--; while(array[j]>x);
            do i++; while(array[i]<x);
            if ( i < j )
                {
                //exchange array[i]<->array[j]
                double tmp = array[i];
                array[i]=array[j];
                array[j]=tmp;
                }
            else
                return j;
            }
        }


    }
