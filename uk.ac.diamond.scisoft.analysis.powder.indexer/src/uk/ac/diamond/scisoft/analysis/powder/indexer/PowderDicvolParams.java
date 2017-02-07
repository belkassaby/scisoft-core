package uk.ac.diamond.scisoft.analysis.powder.indexer;

/**
 * A parameter bean of configurables avaliable for Dicvol
 * 
 * 
 * @author Dean P. Ottewell
 *
 */
public class PowderDicvolParams {
	
	
	
}	
	
	
	
	
	
	
	// CARD 2 N,ITYPE,JC,JT,JH,JO,JM,JTR -8 FREE FORMAT
//	public static final String N = "N"; //TODO: Need intializer elsewhere
//				put("N", "20");
//				// N NUMBER OF LINES USED FOR SEARCHING SOLUTIONS.
//				// (This number is, generally, lower than the
//				// number N_TOTAL of input lines). e.g. N = 20.
//				put("ITYPE", "2"); // 2-theta default
//				// ITYPE SPACING DATA TYPE.
//				// =1 THETA BRAGG ANGLE IN DEGREES.
//				// =2 2-THETA ANGLE IN DEGREES.
//				// =3 D-SPACING IN ANGSTROM UNIT.
//				// =4 Q SPECIFIED IN Q-UNITS AS E+04/D**2.
//				put("JC", "1");
//				// JC =0 CUBIC SYSTEM IS NOT TESTED.
//				// =1 CUBIC SYSTEM IS TESTED.
//				put("JT", "1");
//				// JT =0 TETRAGONAL SYSTEM IS NOT TESTED.
//				// =1 TETRAGONAL SYSTEM IS TESTED.
//				put("JH", "0");
//				// JH =0 HEXAGONAL SYSTEM IS NOT TESTED.
//				// =1 HEXAGONAL SYSTEM IS TESTED.
//				put("JO", "0");
//				// JO =0 ORTHORHOMBIC SYSTEM IS NOT TESTED.
//				// =1 ORTHORHOMBIC SYSTEM IS TESTED.
//				put("JM", "0");
//				// JM =0 MONOCLINIC SYSTEM IS NOT TESTED.
//				// =1 MONOCLINIC SYSTEM IS TESTED.
//				put("JTR", "1");
//				// JTR =0 TRICLINIC SYSTEM IS NOT TESTED.
//				// =1 TRICLINIC SYSTEM IS TESTED.
//
//				// 0. sets to default
//				// CARD 3 AMAX,BMAX,CMAX,VOLMIN,VOLMAX,BEMIN,BEMAX-7 FREE FORMAT
//				put("AMAX", "0.");
//				// AMAX MAXIMUM VALUE OF UNIT CELL DIMENSION A, IN ANGSTROMS.
//				// (IF AMAX= 0.0 DEFAULT= 25. ANGSTROMS)
//				put("BMAX", "0.");
//				// BMAX MAXIMUM VALUE OF UNIT CELL DIMENSION B, IN ANGSTROMS.
//				// (IF BMAX= 0.0 DEFAULT= 25. ANGSTROMS)
//				put("CMAX", "0.");
//				// CMAX MAXIMUM VALUE OF UNIT CELL DIMENSION C, IN ANGSTROMS.
//				// (IF CMAX= 0.0 DEFAULT= 25. ANGSTROMS)
//				put("VOLMIN", "0.");
//				// VOLMIN MINIMUM VOLUME FOR UNIT CELLS IN ANGSTROMS**3.
//				put("VOLMAX", "0."); // TODO: default 4000
//				// VOLMAX MAXIMUM VOLUME FOR UNIT CELLS IN ANGSTROMS**3.
//				// (IF VOLMAX= 0.0 DEFAULT= 2500. ANGSTROMS**3)
//				put("BEMIN", "0."); // TODO: default 90.
//				// BEMIN MINIMUM BETA ANGLE FOR MONOCLINIC CELLS IN DEGREES
//				// (IF BEMIN= 0.0 DEFAULT= 90. DEGREES).
//				put("BEMAX", "0."); // TODOL default 125.
//				// BEMAX MAXIMUM BETA ANGLE FOR MONOCLINIC CELLS IN DEGREES
//				// (IF BEMAX= 0.0 DEFAULT= 125. DEGREES).
//
//				// CARD 4 WAVE,POIMOL,DENS,DELDEN-4 FREE FORMAT
//				put("WAVE", "0."); // TODO: Paramterize wavelength
//				// WAVE WAVELENGTH IN ANGSTROMS (DEFAULT=0.0 IF CU K ALPHA1).
//				put("POIMOL", "0.");
//				// POIMOL MOLECULAR WEIGHT OF ONE FORMULA UNIT IN A.M.U.
//				// (DEFAULT =0.0 IF FORMULA WEIGHT NOT KNOWN).
//				put("DENS", "0.");
//				// DENS MEASURED DENSITY IN G CM(**-3)
//				// (DEFAULT =0.0 IF DENSITY NOT KNOWN).
//				put("DELDEN", "0.");
//				// DELDEN ABSOLUTE ERROR IN MEASURED DENSITY.
//				// (DEFAULT =0.0 IF DENSITY NOT KNOWN).
//
//				// CARD 5 EPS,FOM,N_IMP,ZERO_S,ZERO_REF,OPTION-6 FREE FORMAT
//				put("EPS", "0.03"); // Defaults 0.03 when 2THETA used
//				// EPS =0.0 THE ABSOLUTE ERROR ON EACH OBSERVED LINE
//				// IS TAKEN TO 0.03 DEG. 2THETA (DEFAULT VALUE)
//				// WHATEVER THE SPACING DATA TYPE (ITYPE IN CARD 2).
//				// =1.0 THE ABSOLUTE ERROR ON EACH OBSERVED LINE IS
//				// INPUT INDIVIDUALLY FROM CARD 6, AFTER THE
//				// OBSERVED 'D(I)' ON THE SAME LINE, ACCORDING
//				// TO THE SPACING DATA UNIT (e.g. 18.678 0.018 in
//				// deg. 2theta)
//				// EPS NE 0.0 AND 1.0
//				// THE ABSOLUTE ERROR IS TAKEN AS A CONSTANT
//				// (= EPS),IN DEG. 2THETA, WHATEVER THE SPACING
//				// DATA TYPE (ITYPE IN CARD 2) (e.g. 0.02, which will
//				// apply to all input lines).
//
//				put("FOM", "0."); // TODO: Default 10
//				// FOM LOWER FIGURE OF MERIT M(N) REQUIRED FOR PRINTED
//				// SOLUTION(S) (DEFAULT=0.0 M(N)=10.0).
//				put("N_IMP", "0");
//				// N_IMP MAXIMUM NUMBER OF IMPURITY/SPURIOUS LINES ACCEPTED AMONG
//				// THE FIRST N LINES [N_IMP takes into account both
//				// impurity lines and peak positions out of the input
//				// absolute angular error EPS].
//				// IF N_IMP <0 THE SEARCH STARTS WITH ZERO IMPURITY LINES,
//				// THEN, IT CONTINUES WITH ONE IMPURITY LINE, AND
//				// SO ON UNTIL 'N_IMP' IMPURITY LINES IS REACHED.
//				put("ZERO_S", "0");
//				// ZERO_S A PRIORI SEARCH FOR A ZERO-POINT ERROR IN INPUT DATA.
//				// =0 NO SEARCH
//				// =1 SEARCH
//				// IF ZERO_S NE 0 OR 1, THEN ZERO_S REPRESENTS A KNOWN
//				// ZERO CORRECTION (e.g. -0.10) IN DEG. 2THETA.
//				put("ZERO_REF", "0");
//				// ZERO_REF =0 NO 'ZERO-POINT' LEAST-SQUARES REFINEMENT.
//				// =1 'ZERO-POINT' LEAST-SQUARES REFINEMENT.
//				//
//				put("OPTION", "0"); // exhaustive search divol06 default
//				// OPTION =0 DICVOL04 OPTION (OPTIMIZED STRATEGY SEARCH WITH
//				// DECREASING CELL VOLUMES).
//				// =1 OPTION WITH EXTENDED (EXHAUSTIVE) SEARCH IN VOLUME
//				// DOMAINS CONTAINING MATHEMATICAL SOLUTION(S)
//				// (LONGER CPU TIMES).
//				// IF OPTION IS OMITTED, DEFAULT IS DICVOL04.

