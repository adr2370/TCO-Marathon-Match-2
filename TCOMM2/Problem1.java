import java.io.*;
import java.util.*;
class Problem1
{
    public static void main (String [] args) throws Exception 
    {
        BufferedReader f = new BufferedReader(new FileReader("input.java"));
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("output.java")));
        StringTokenizer st = new StringTokenizer(f.readLine());
        int T=Integer.parseInt(st.nextToken());
        for(int t=0;t<T;t++) {
            st=new StringTokenizer(f.readLine());
            int N=Integer.parseInt(st.nextToken());
            int[] numbers=new int[N];
            int totalSum=0;
            for(int i=0;i<N;i++) {
                numbers[i]=Integer.parseInt(st.nextToken());
                totalSum+=numbers[i];
            }
            boolean finished=false;
            HashMap<Integer, ArrayList<String>> totalSums=new HashMap<Integer, ArrayList<String>>();
            for(int i=0;!finished&&i<Math.pow(2,N);i++) {
                if(i%100000==0) System.out.println(i);
                String s=Integer.toBinaryString(i);
                while(s.length()<N) s="0"+s;
                int sum1=0;
                String first="";
                for(int j=0;!finished&&j<N;j++) {
                    if(s.charAt(j)=='0') {
                        sum1+=numbers[j];
                        first=first+numbers[j]+" ";
                    }
                }
                if(sum1<=(totalSum+1)/2&&totalSums.containsKey(sum1)) {
                    ArrayList<String> currStrings=totalSums.get(sum1);
                    int startingSize=currStrings.size();
                    for(int j=0;j<startingSize;j++) {
                        String s1=first.substring(0,first.length()-1);
                        String s2=currStrings.get(j);
                        ArrayList<Integer> allInts=new ArrayList<Integer>();
                        st=new StringTokenizer(s1);
                        boolean duplicates=false;
                        while(!duplicates&&st.hasMoreTokens()) {
                            int nextInt=Integer.parseInt(st.nextToken());
                            if(allInts.contains(nextInt)) {
                                duplicates=true;
                            } else {
                                allInts.add(nextInt);
                            }
                        }
                        st=new StringTokenizer(s2);
                        while(!duplicates&&st.hasMoreTokens()) {
                            int nextInt=Integer.parseInt(st.nextToken());
                            if(allInts.contains(nextInt)) {
                                duplicates=true;
                            } else {
                                allInts.add(nextInt);
                            }
                        }
                        if(!duplicates) {
                            out.println("Case #"+(t+1)+":");
                            out.println(s1);
                            out.println(s2);
                            finished=true;
                        }
                    }
                    if(!finished) {
                        currStrings.add(first.substring(0,first.length()-1));
                    }
                    totalSums.put(sum1, currStrings);
                } else if(sum1<=(totalSum+1)/2) {
                    ArrayList<String> curr=new ArrayList<String>();
                    curr.add(first.substring(0,first.length()-1));
                    totalSums.put(sum1, curr);
                }
            }
            System.err.println("Case #"+(t+1));
            if(!finished) {
                out.println("Case #"+(t+1)+": \nImpossible");
            }
        }
        out.close();
        System.exit(0);
    }
}