import java.util. * ;
import java.lang. * ;
import java.math. * ;
import java.io. * ;
public class TwistedGame
{
	final int dr[] = { -1, 0, 1, 0 }, dc[] = { 0, 1, 0, -1 };
	int nTiles;
	HashMap<Integer, int[]> placedTiles;
	// i - index of placement, second index is record:
	// 0 - the actual placement,
	// 1 - the place of the active tile it's adjacent to,
	// 2 - the side of the active tile it's adjacent to
	int[] validPlacements;
	int tileIndex;
	Chain[] chains;
	int row, col, rot; // current move params
	String errorMessage;
	int[] tilePlace;
	int surfaceArea;
	ArrayList<String> permutations;
	ArrayList<Chain> oChains;

	class Chain {
		public int chainLength;
		// active contacts are the ones to be filled with the next tile (they
		// point beyond the chain)
		public int[][] activeContacts;
		public boolean looped;

		public Chain() {
			chainLength = 0;
			looped = false;
		}

		public Chain(int contactPlace, int contactIndex) {
			chainLength = 0;
			looped = false;
			activeContacts = new int[2][2];

			activeContacts[0][0] = contactPlace;
			activeContacts[0][1] = contactIndex;

			activeContacts[1][0] = adjacentPlace(contactPlace, contactIndex / 2);
			activeContacts[1][1] = matchingContact(contactIndex);

			// and extend both contacts
			extend(0);
			extend(1);
		}

		public void extend(int end) {
			if (looped) {
				return;
			}
			// knowing that the state of the board changed, try to extend the
			// chain from the given end
			while (true) {
				// check whether there is a tile placed at the next cell of this
				// contact
				if (!placedTiles.containsKey(activeContacts[end][0])) {
					break;
				}

				// replace this contact with the one it's connected to
				activeContacts[end][1] = connectedContact(
					placedTiles.get(activeContacts[end][0]),
					activeContacts[end][1]);
				// length is incremented when the wire connection is used
				++chainLength;

				// check whether the chain looped - connected to its other
				// active contact
				if (activeContacts[end][0] == activeContacts[1 - end][0]
				&& activeContacts[end][1] == activeContacts[1 - end][1]) {
					looped = true;
					break;
				}

				int dir = activeContacts[end][1] / 2;
				int nextPlace = adjacentPlace(activeContacts[end][0], dir);
				activeContacts[end][0] = nextPlace;
				activeContacts[end][1] = matchingContact(activeContacts[end][1]);
			}
		}
		public boolean extendToLoop(int end) {
			if (looped) {
				return true;
			}
			// knowing that the state of the board changed, try to extend the
			// chain from the given end
			int[][] tempActiveContacts=new int[2][2];
			for(int i=0;i<2;i++) for(int j=0;j<2;j++) tempActiveContacts[i][j]=activeContacts[i][j];
			while (true) {
				// check whether there is a tile placed at the next cell of this
				// contact
				if (!placedTiles.containsKey(tempActiveContacts[end][0])) {
					break;
				}

				// replace this contact with the one it's connected to
				tempActiveContacts[end][1] = connectedContact(
					placedTiles.get(tempActiveContacts[end][0]),
					tempActiveContacts[end][1]);
				// length is incremented when the wire connection is used
				++chainLength;

				// check whether the chain looped - connected to its other
				// active contact
				if (tempActiveContacts[end][0] == tempActiveContacts[1 - end][0]
				&& tempActiveContacts[end][1] == tempActiveContacts[1 - end][1]) {
					looped = true;
					break;
				}

				int dir = tempActiveContacts[end][1] / 2;
				int nextPlace = adjacentPlace(tempActiveContacts[end][0], dir);
				tempActiveContacts[end][0] = nextPlace;
				tempActiveContacts[end][1] = matchingContact(tempActiveContacts[end][1]);
			}
			return looped;
		}
	}

	// ----------------------------------------------------------------------------------
	int[] rotateTile(int[] tile, int rotation) {
		int[] rotatedTile = new int[8];
		for (int i = 0; i < 8; ++i) {
			rotatedTile[i] = (tile[i] + 2 * rotation) % 8;
		}
		return rotatedTile;
	}

	// ----------------------------------------------------------------------------------
	int cellPlace(int row, int col) {
		return row * (2 * nTiles + 1) + col;
	}

	// ----------------------------------------------------------------------------------
	int adjacentPlace(int place, int dir) {
		return place + dr[dir] * (2 * nTiles + 1) + dc[dir];
	}

	// ----------------------------------------------------------------------------------
	int matchingContact(int contactIndex) {
		// if two tiles are adjacent, what's the index of the matching contact
		if (contactIndex < 2 || contactIndex == 4 || contactIndex == 5) {
			return 5 - contactIndex;
		}
		return 9 - contactIndex;
	}

	// ----------------------------------------------------------------------------------
	int connectedContact(int[] tile, int contactIndex) {
		// given the tile description, return the contact connected to the given
		// one
		for (int i = 0; i < 8; ++i) {
			if (tile[i] == contactIndex) {
				return tile[i + 1 - 2 * (i % 2)];
			}
		}
		return -1;
	}

	// ----------------------------------------------------------------------------------
	boolean isValidMove() {
		if (rot < 0 || rot > 3) {
			errorMessage = "ROT must be between 0 and 3, inclusive.";
			return false;
		}
		int movePlace = cellPlace(row, col);
		if (placedTiles.containsKey(movePlace)) {
			errorMessage = "Placed tiles must not overlap.";
			return false;
		}
		for (int i = 0; i < 4; ++i) {
			if (validPlacements[i] == movePlace) {
				return true;
			}
		}
		errorMessage = "Invalid tile position.";
		return false;
	}

	// ----------------------------------------------------------------------------------
	boolean isValidReturn(String move) {
		// must be formatted as "ROW COL ROT"
		errorMessage = "";
		if (move.equals("GIVE UP")) {
			return true;
		}
		try {
			String[] sp = move.split(" ");
			if (sp.length != 3) {
				errorMessage = "Your return must be formatted as \"ROW COL ROT\".";
				return false;
			}
			row = Integer.parseInt(sp[0]);
			col = Integer.parseInt(sp[1]);
			rot = Integer.parseInt(sp[2]);
		} catch (Exception e) {
			errorMessage = "Your return must be formatted as \"ROW COL ROT\".";
			return false;
		}
		return isValidMove();
	}
	ArrayList < Integer > toList(int[] a) {
		ArrayList < Integer > ans = new ArrayList < Integer > ();
		for (int i = 0; i < a.length; i++) ans.add(a[i]);
		return ans;
	}
	int init(int N, int[] firstTile) {
		tilePlace = new int[N];
		oChains=new ArrayList<Chain>();
		nTiles=N;
		row = N;
		col = N;
		rot = 0;
		tilePlace[tileIndex] = cellPlace(row, col);
		placedTiles = new HashMap<Integer, int[]>();
		placedTiles.put(tilePlace[tileIndex], firstTile);
		for(int gothrough=0;gothrough<8;gothrough+=2) oChains.add(new Chain(tilePlace[tileIndex],firstTile[gothrough]));
		validPlacements = new int[4];
		for (int i = 0; i < 4; ++i) {
			validPlacements[i] = cellPlace(row + dr[i], col + dc[i]);
		}
		surfaceArea=4;
		permuteAllPermutations();
		return 0;
	}
	String placeTile(int[] tile) {
		++tileIndex;
		if(tileIndex==1) {
			int maxScore=1;
			String bestAns="GIVE UP";
			Chain[] replaceChains=new Chain[2];
			boolean canWithoutLoop=true;
			for(int i=0;i<4;i++) {
				for(int j=0;j<4;j++) {
					if(validPlacements[i]>0&&!placedTiles.containsKey(validPlacements[i])) {
						long time1=System.nanoTime();
						row=validPlacements[i]/(2*nTiles+1);
						col=validPlacements[i]%(2*nTiles+1);
						rot=j;
						String ansMove=row+" "+col+" "+j;
						tilePlace[tileIndex] = cellPlace(row, col);
						for(int gothrough=0;gothrough<8;gothrough+=2) oChains.add(new Chain(tilePlace[tileIndex],tile[gothrough]));
						placedTiles.put(tilePlace[tileIndex], rotateTile(tile, rot));
						// and extend chains taking into account the new tile
						long time2=System.nanoTime();
						Chain[] tempChains = new Chain[2];
						int k,l;
						for (k = 0; k < 4; ++k) {
							if (validPlacements[k] == tilePlace[tileIndex]) {
								break;
							}
						}
						// the contacts on this side are: side * 2 (+1) for first tile
						for (l = 0; l < 2; ++l) {
							tempChains[l] = new Chain(tilePlace[0], k * 2 + l);
						}
						if(!(tempChains[0].looped&&tempChains[1].looped)&&Math.max(tempChains[0].chainLength,tempChains[1].chainLength)>=maxScore) {
							replaceChains=tempChains;
							maxScore=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
							bestAns=ansMove;
						}
						placedTiles.remove(tilePlace[tileIndex]);
						long time3=System.nanoTime();
						//System.err.println((time2-time1)+" "+(time3-time2));
					}
				}
			}
			chains=replaceChains;
			if(bestAns.equals("GIVE UP")) {
				bestAns=validPlacements[0]/(2*nTiles+1)+" "+validPlacements[0]%(2*nTiles+1)+" "+0;
			}
			StringTokenizer st=new StringTokenizer(bestAns);
			row=Integer.parseInt(st.nextToken());
			col=Integer.parseInt(st.nextToken());
			rot=Integer.parseInt(st.nextToken());
			tilePlace[tileIndex] = cellPlace(row, col);
			placedTiles.put(tilePlace[tileIndex], rotateTile(tile, rot));
			surfaceArea=6;
			return bestAns;
		} else {
			int i,j;
			// update valid placements
			if(chains[0].looped&&chains[1].looped) {
				validPlacements = new int[4];
				for (i = 0; i < 4; ++i) {
					validPlacements[i] = cellPlace(nTiles + dr[i], nTiles + dc[i]);
				}
				chains = new Chain[2];
				int k,l;
				for (k = 0; k < 4; ++k) {
					if (validPlacements[k] == tilePlace[1]) {
						break;
					}
				}
				// the contacts on this side are: side * 2 (+1) for first tile
				for (l = 0; l < 2; ++l) {
					chains[l] = new Chain(tilePlace[0], k * 2 + l);
				}
				if(chains[0].looped&&chains[1].looped) {
					System.err.println("LOOPED");
					return chains[0].activeContacts[0][0]/(2*nTiles+1)+" "+chains[0].activeContacts[0][0]%(2*nTiles+1)+" 0";
				}
			}
			for (i = 0; i < 4; ++i) {
				if (chains[i / 2].looped) {
					validPlacements[i] = -1;
				} else {
					validPlacements[i] = chains[i / 2].activeContacts[i % 2][0];
				}
			}
			int maxScore=Integer.MIN_VALUE;
			int minsurfaceArea=surfaceArea+4;
			for(int a=0;a<4;a++) {
				if(placedTiles.containsKey(adjacentPlace(validPlacements[0],a))) {
					minsurfaceArea-=2;
				}
			}
			String bestAns="GIVE UP";
			int retryCount=0;
			long time1=0,time2=0,time3=0;
			while(retryCount<7) {
				Chain[] replaceChains=new Chain[2];
				boolean canWithoutLoop=true;
				for(i=0;i<4;i++) {
					if(validPlacements[i]>0&&!placedTiles.containsKey(validPlacements[i])) {
						for(j=0;j<4;j++) {
							row=validPlacements[i]/(2*nTiles+1);
							col=validPlacements[i]%(2*nTiles+1);
							rot=j;
							String ansMove=row+" "+col+" "+j;
							tilePlace[tileIndex] = cellPlace(row, col);
							placedTiles.put(tilePlace[tileIndex], rotateTile(tile, rot));
							// and extend chains taking into account the new tile
							Chain[] tempChains = new Chain[2];
							for(int k=0;k<2;k++) {
								tempChains[k]=new Chain();
								tempChains[k].chainLength=chains[k].chainLength;
								tempChains[k].activeContacts=new int[chains[k].activeContacts.length][chains[k].activeContacts[0].length];
								for(int l=0;l<chains[k].activeContacts.length;l++) for(int m=0;m<chains[k].activeContacts[0].length;m++) {
									tempChains[k].activeContacts[l][m]=chains[k].activeContacts[l][m];
								}
								tempChains[k].looped=chains[k].looped;
							}
							for (int a = 0; a < 4; ++a) {
								tempChains[a / 2].extend(a % 2);
							}
							if(tempChains[0].looped&&tempChains[1].looped) {
								for(int b=0;b<2;b++) {
									int[][] a=chains[b].activeContacts;
									//System.err.println(a[0][0]+" "+a[0][1]+" "+a[1][0]+" "+a[1][1]);
								}
								//for(int b=0;b<8;b++) System.err.print(tile[b]+" ");
								//System.err.println();
							}
							int tempSurfaceArea=surfaceArea;
							tempSurfaceArea+=4;
							for(int a=0;a<4;a++) {
								if(placedTiles.containsKey(adjacentPlace(validPlacements[i],a))) {
									tempSurfaceArea-=2;
								}
							}
							//check if next move coulde be your last
							//if so, don't do it
							boolean nextCouldBeLast=false;
							tileIndex++;
							if(tileIndex<nTiles
							&&(tempChains[0].activeContacts[0][0]==tempChains[0].activeContacts[1][0])) {
								int[] tempValidPlacements=new int[4];
								for (int abcd = 0; abcd < 4; ++abcd) {
									tempValidPlacements[abcd]=validPlacements[abcd];
									if (chains[abcd / 2].looped) {
										tempValidPlacements[abcd] = -1;
									} else {
										tempValidPlacements[abcd] = tempChains[abcd / 2].activeContacts[abcd % 2][0];
									}
								}
								boolean anyNotLooped=false;
								for(int spot=0;!nextCouldBeLast&&spot<4;spot++) {
									if(tempValidPlacements[spot]>0&&!placedTiles.containsKey(validPlacements[spot])) {
										for(int per=0;!anyNotLooped&&per<permutations.size();per++) {
											int[] currTile=new int[8];
											for(int l=0;l<8;l++) {
												currTile[l]=permutations.get(per).charAt(l)-'0';
											}
											row=tempValidPlacements[spot]/(2*nTiles+1);
											col=tempValidPlacements[spot]%(2*nTiles+1);
											rot=0;
											tilePlace[tileIndex] = cellPlace(row, col);
											placedTiles.put(tilePlace[tileIndex], rotateTile(currTile, rot));
											boolean[] looped=new boolean[2];
											for (int a = 0; a < 4; ++a) {
												looped[a/2]=tempChains[a / 2].extendToLoop(a % 2);
											}
											if(!looped[0]||!looped[1]) {
												anyNotLooped=true;
											}
											placedTiles.remove(tilePlace[tileIndex]);
										}
									}
									if(!anyNotLooped) nextCouldBeLast=true;
								}
							}
							tileIndex--;
							row=validPlacements[i]/(2*nTiles+1);
							col=validPlacements[i]%(2*nTiles+1);
							rot=j;
							//done checking
							//check how many loops 
							//final int dr[] = { -1, 0, 1, 0 }, dc[] = { 0, 1, 0, -1 };
							int loops=0;
							placedTiles.remove(tilePlace[tileIndex]);
							if(chains[i / 2].activeContacts[i % 2][1]/2!=2&&placedTiles.containsKey(tilePlace[tileIndex]+2*nTiles+1)) {
								Chain c = new Chain(tilePlace[tileIndex]+2*nTiles+1, 0);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
								c = new Chain(tilePlace[tileIndex]+2*nTiles+1, 1);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
							}
							if(chains[i / 2].activeContacts[i % 2][1]/2!=3&&placedTiles.containsKey(tilePlace[tileIndex]-1)) {
								Chain c = new Chain(tilePlace[tileIndex]-1, 2);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
								c = new Chain(tilePlace[tileIndex]-1, 3);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
							}
							if(chains[i / 2].activeContacts[i % 2][1]/2!=0&&placedTiles.containsKey(tilePlace[tileIndex]-2*nTiles-1)) {
								Chain c = new Chain(tilePlace[tileIndex]-2*nTiles-1, 4);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
								c = new Chain(tilePlace[tileIndex]-2*nTiles-1, 5);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
							}
							if(chains[i / 2].activeContacts[i % 2][1]/2!=1&&placedTiles.containsKey(tilePlace[tileIndex]+1)) {
								Chain c = new Chain(tilePlace[tileIndex]+1, 6);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
								c = new Chain(tilePlace[tileIndex]+1, 7);
								if(c.activeContacts[0][0]==tilePlace[tileIndex]
								&&c.activeContacts[1][0]==tilePlace[tileIndex]) {
									int fContact=c.activeContacts[0][1];
									int sContact=c.activeContacts[1][1];
									int[] rotated=rotateTile(tile, rot);
									for(int contacts=0;contacts<rotated.length;contacts+=2) {
										if(rotated[contacts]==fContact&&rotated[contacts+1]==sContact) {
											loops++;
										} else if(rotated[contacts+1]==fContact&&rotated[contacts]==sContact) {
											loops++;
										}
									}
									loops++;
								}
							}
							placedTiles.put(tilePlace[tileIndex],rotateTile(tile, rot));
							//done with loops
							int newScore=moveScore(loops,nextCouldBeLast,retryCount,tileIndex,tempChains,tempSurfaceArea,minsurfaceArea);
							if(!(tempChains[0].looped&&tempChains[1].looped)&&maxScore<newScore) {
								maxScore=newScore;
								replaceChains=tempChains;
								bestAns=ansMove;
								minsurfaceArea=tempSurfaceArea;
							}
							placedTiles.remove(tilePlace[tileIndex]);
						}
					}
				}
				if(!bestAns.equals("GIVE UP")) {
					StringTokenizer st=new StringTokenizer(bestAns);
					row=Integer.parseInt(st.nextToken());
					col=Integer.parseInt(st.nextToken());
					rot=Integer.parseInt(st.nextToken());
					tilePlace[tileIndex] = cellPlace(row, col);
					placedTiles.put(tilePlace[tileIndex], rotateTile(tile, rot));
					chains=replaceChains;
					surfaceArea=minsurfaceArea;
					return bestAns;
				}
				retryCount++;
			}
			if(bestAns.equals("GIVE UP")) {
				bestAns=validPlacements[0]/(2*nTiles+1)+" "+validPlacements[0]%(2*nTiles+1)+" "+0;
			}
			return bestAns;
		}
	}
	int moveScore(int loops,boolean nextCouldBeLast,int retryCount,int tileIndex,Chain[] tempChains,int tempSurfaceArea,int minsurfaceArea) {
		//Attempt 1 Score Average = 11622.64
		//Notes: A ton of not finishing
		/*
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
		*/
		//Attempt 2 Score Average = 19095.65
		//Notes: A ton of not finishing
		/*
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
		if(nextCouldBeLast) score-=1000000;
		*/
		//Attempt 3 Score Average = 19095.65
		//Notes: Almost all not finishing
		/*
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
		if(nextCouldBeLast) score-=1000000;
		if(somethingLooped) score-=100000;
		*/
		//Attempt 4 Score Average = 23320.57
		//Notes: A lot of not finishing
		/*
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
		if(tileIndex<1000) {
			score=tempSurfaceArea;
		}
		if(nextCouldBeLast) score-=1000000;
		*/
		//Attempt 5 Score Average = 10186.75
		//Notes: All finishing
		/*
		int score=-Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1));
		score=Math.min(score,-Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1)));
		if(nextCouldBeLast) score-=10000000;
		*/
		//Attempt 6 Score Average = 14508.41
		//Notes: Almost All finishing
		/*
		int score=-Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1));
		score=Math.min(score,-Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1)));
		score+=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
		if(nextCouldBeLast) score-=10000000;
		*/
		//Attempt 7 Score Average = 29869.65
		//Notes: Almost All finishing
		/*
		int score=-Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1));
		score=Math.min(score,-Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1)));
		score+=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*10;
		if(nextCouldBeLast) score-=10000000;
		*/
		//Attempt 8 Score Average = 29822.03
		//Notes: Almost All finishing
		/*
		int score=-Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1));
		score=Math.min(score,-Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1)));
		score+=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*20;
		if(nextCouldBeLast) score-=10000000;
		*/
		//Attempt 9 Score Average = 28279.54
		//Notes: Almost All finishing
		/*
		int score=-Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1));
		score=Math.min(score,-Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1)));
		score+=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*10;
		if(tileIndex<1000) {
			score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength);
			score+=tempSurfaceArea*10;
		}
		if(nextCouldBeLast) score-=10000000;
		*/
		//Attempt 10 Score Average = 28100.42
		//Notes: Almost All finishing
		/*
		int score=Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1));
		score=Math.min(score,Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))+Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1)));
		score+=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*1000;
		if(nextCouldBeLast) score-=10000000;
		*/
		//Attempt 11 Score Average = 32586.89
		//Notes: Almost All finishing
		/*
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*1000-tempSurfaceArea;
		if((Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))>10
				&&Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1))>10
				||Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1))>10
				&&Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))>10)) {
			score+=100000;
		}
		if(tileIndex<100)
			score=tempSurfaceArea;
		if(nextCouldBeLast) score-=1000000;
		*/
		//Attempt 12 Score Average = 32877.28
		//Notes: Almost All finishing
		/*
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*1000-tempSurfaceArea;
		if((Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))>10
				&&Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1))>10
				||Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1))>10
				&&Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))>10)) {
			score+=100000;
		}
		if(tileIndex<200)
			score=tempSurfaceArea;
		if(nextCouldBeLast) score-=1000000;
		*/
		//Attempt 13 Score Average = 34182.61
		//Notes: Almost All finishing
		int score=Math.max(tempChains[0].chainLength,tempChains[1].chainLength)*1000-tempSurfaceArea;
		if((Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))>10
				&&Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1))>10
				||Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1))>10
				&&Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))>10)) {
			score+=100000;
		}
		if(tileIndex<300)
			score=tempSurfaceArea;
		if(nextCouldBeLast) score-=1000000;
		return score;
	}
	boolean shouldMoveHelper(int retryCount,int tileIndex,Chain[] tempChains,int tempSurfaceArea,int minsurfaceArea,int maxScore) {
		if(retryCount==1||tileIndex<100||(Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))>10
			&&Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1))>10
			||Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1))>10
			&&Math.abs(tempChains[1].activeContacts[0][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1))>10)
			/*||tileIndex>=100
			&&(Math.abs(tempChains[0].activeContacts[0][0]%(2*nTiles+1)-tempChains[0].activeContacts[1][0]%(2*nTiles+1))>=
			Math.abs(chains[0].activeContacts[0][0]%(2*nTiles+1)-chains[0].activeContacts[1][0]%(2*nTiles+1))
			&&Math.abs(tempChains[0].activeContacts[0][0]/(2*nTiles+1)-tempChains[0].activeContacts[1][0]/(2*nTiles+1))>=
			Math.abs(chains[0].activeContacts[0][0]/(2*nTiles+1)-chains[0].activeContacts[1][0]/(2*nTiles+1))
			||Math.abs(tempChains[1].activeContacts[0][0]%(2*nTiles+1)-tempChains[1].activeContacts[1][0]%(2*nTiles+1))>=
			Math.abs(chains[1].activeContacts[0][0]%(2*nTiles+1)-chains[1].activeContacts[1][0]%(2*nTiles+1))
			&&Math.abs(tempChains[1].activeContacts[1][0]/(2*nTiles+1)-tempChains[1].activeContacts[1][0]/(2*nTiles+1))>=
			Math.abs(chains[1].activeContacts[1][0]/(2*nTiles+1)-chains[1].activeContacts[1][0]/(2*nTiles+1)))*/)
		{
			if(!(tempChains[0].looped&&tempChains[1].looped)&&((tileIndex<100&&tempSurfaceArea>=minsurfaceArea)
				||Math.max(tempChains[0].chainLength,tempChains[1].chainLength)>maxScore
				||Math.max(tempChains[0].chainLength,tempChains[1].chainLength)==maxScore
			&&(tileIndex<1&&tempSurfaceArea>=minsurfaceArea||tempSurfaceArea<=minsurfaceArea))) {
				return true;
			}
		}
		return false;
	}
	boolean shouldMove(boolean somethingLooped,boolean nextCouldBeLast,int retryCount,int tileIndex,Chain[] tempChains,int tempSurfaceArea,int minsurfaceArea,int maxScore) {
		boolean changeIt=false;
		if(retryCount==0) {
			if(!somethingLooped&&!nextCouldBeLast&&shouldMoveHelper(retryCount,tileIndex,tempChains,tempSurfaceArea,minsurfaceArea,maxScore)) {
				changeIt=true;
			}
		} else if(retryCount==1) {
			if(!nextCouldBeLast&&shouldMoveHelper(retryCount,tileIndex,tempChains,tempSurfaceArea,minsurfaceArea,maxScore)) {
				changeIt=true;
			}
		} else if(retryCount==2) {
			if(!nextCouldBeLast) {
				changeIt=true;
			}
		}
		else if(retryCount==3) {
			if(!somethingLooped&&shouldMoveHelper(retryCount,tileIndex,tempChains,tempSurfaceArea,minsurfaceArea,maxScore)) {
				changeIt=true;
			}
		}
		else if(retryCount==4) {
			if(shouldMoveHelper(retryCount,tileIndex,tempChains,tempSurfaceArea,minsurfaceArea,maxScore)) {
				changeIt=true;
			}
		}
		else if(retryCount==5) {
			if(!somethingLooped) {
				changeIt=true;
			}
		}
		else {
			changeIt=true;
		}
		return changeIt;
	}
	public static void main(String[] args) throws Exception {
		BufferedReader in =new BufferedReader(new InputStreamReader(System. in ));
		StringTokenizer st = new StringTokenizer( in .readLine());
		int N = Integer.parseInt(st.nextToken());
		int[] firstTile = new int[8];
		for (int i = 0; i < 8; i++) {
			if (!st.hasMoreTokens()) {
				String next = in.readLine();
				if (next != null) st = new StringTokenizer(next);
				else return;
			}
			firstTile[i] = Integer.parseInt(st.nextToken());
		}
		TwistedGame tg = new TwistedGame();
		tg.init(N, firstTile);
		if (!st.hasMoreTokens()) {
			String next = in.readLine();
			if (next != null) st = new StringTokenizer(next);
			else return;
		}
		while (st.hasMoreTokens()) {
			int[] tile = new int[8];
			for (int i = 0; i < 8; i++) {
				tile[i] = Integer.parseInt(st.nextToken());
			}
			String ret = tg.placeTile(tile);
			//System.err.println(ret);
			System.out.println(ret);
			if (!st.hasMoreTokens()) {
				String next = in.readLine();
				if (next != null) st = new StringTokenizer(next);
				else return;
			}
		}
	}
	void permuteAllPermutations() {
		permutations=new ArrayList<String>();
		permutations.add("01234567");permutations.add("01234657");permutations.add("01234756");permutations.add("01243567");permutations.add("01243657");permutations.add("01243756");permutations.add("01253467");permutations.add("01253647");permutations.add("01253746");permutations.add("01263457");permutations.add("01263547");permutations.add("01263745");permutations.add("01273456");permutations.add("01273546");permutations.add("01273645");permutations.add("02134567");permutations.add("02134657");permutations.add("02134756");permutations.add("02143567");permutations.add("02143657");permutations.add("02143756");permutations.add("02153467");permutations.add("02153647");permutations.add("02153746");permutations.add("02163457");permutations.add("02163547");permutations.add("02163745");permutations.add("02173456");permutations.add("02173546");permutations.add("02173645");permutations.add("03124567");permutations.add("03124657");permutations.add("03124756");permutations.add("03142567");permutations.add("03142657");permutations.add("03142756");permutations.add("03152467");permutations.add("03152647");permutations.add("03152746");permutations.add("03162457");permutations.add("03162547");permutations.add("03162745");permutations.add("03172456");permutations.add("03172546");permutations.add("03172645");permutations.add("04123567");permutations.add("04123657");permutations.add("04123756");permutations.add("04132567");permutations.add("04132657");permutations.add("04132756");permutations.add("04152367");permutations.add("04152637");permutations.add("04152736");permutations.add("04162357");permutations.add("04162537");permutations.add("04162735");permutations.add("04172356");permutations.add("04172536");permutations.add("04172635");permutations.add("05123467");permutations.add("05123647");permutations.add("05123746");permutations.add("05132467");permutations.add("05132647");permutations.add("05132746");permutations.add("05142367");permutations.add("05142637");permutations.add("05142736");permutations.add("05162347");permutations.add("05162437");permutations.add("05162734");permutations.add("05172346");permutations.add("05172436");permutations.add("05172634");permutations.add("06123457");permutations.add("06123547");permutations.add("06123745");permutations.add("06132457");permutations.add("06132547");permutations.add("06132745");permutations.add("06142357");permutations.add("06142537");permutations.add("06142735");permutations.add("06152347");permutations.add("06152437");permutations.add("06152734");permutations.add("06172345");permutations.add("06172435");permutations.add("06172534");permutations.add("07123456");permutations.add("07123546");permutations.add("07123645");permutations.add("07132456");permutations.add("07132546");permutations.add("07132645");permutations.add("07142356");permutations.add("07142536");permutations.add("07142635");permutations.add("07152346");permutations.add("07152436");permutations.add("07152634");permutations.add("07162345");permutations.add("07162435");permutations.add("07162534");
	}
}