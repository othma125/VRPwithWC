
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class Motion {
    private final int Index1,Index2;
    
    Motion(int index1,int index2){
        this.Index1=index1;
        this.Index2=index2;
    }
    
    void Swap(int[] array){
        if(this.Index1==this.Index2)
           return;
        int aux=array[this.Index1];
        array[this.Index1]=array[this.Index2];
        array[this.Index2]=aux;
    }  
    
    void _2opt(int[] array){
        if(this.Index1<this.Index2)
            for(int k=this.Index1,l=this.Index2;k<l;k++,l--)
                new Motion(k,l).Swap(array);
    }
    
    void Insertion(int[] array){
        if(this.Index1<this.Index2){
            int aux=array[this.Index2];
            for(int k=this.Index2;k>this.Index1;k--)
                array[k]=array[k-1];        
            array[this.Index1]=aux;
        }
    }
    
    void InverseInsertion(int[] array){
        if(this.Index1<this.Index2){
            int aux=array[this.Index1];
            for(int k=this.Index1;k<this.Index2;k++)
                array[k]=array[k+1];
            array[this.Index2]=aux;
        }
    }
}