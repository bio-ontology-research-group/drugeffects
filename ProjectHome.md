We investigate the use of mouse model phenotypes for drug
repurposing. To achieve this goal, we first integrate mouse model
phenotypes and drug effects, and then systematically compare the
phenotypic similarity between mouse models and drug effect profiles.
We find a high similarity between phenotypes resulting from loss-of-function mutations and drug effects resulting from the inhibition of a
protein through a drug action, and demonstrate how this approach
can be used to suggest candidate drug targets.

Here, we provide a link to the raw data, including intermediate data tables (http://phenomebrowser.net/drugeffect-data.tar.bz2 ; includes a README file describing the content of each file and directory in the archive). Additionally, we provide the code we used to perform our analysis in the SVN. A large part of our analysis relies on the [PhenomeNET](http://phenomebrowser.net) system, and parts of the code we used is hosted in the [PhenomeNET project's SVN](https://code.google.com/p/phenomeblast/). In particular, the code to generate the similarity matrix using our non-symmetrical measure of similarity is available at https://code.google.com/p/phenomeblast/source/browse/trunk/phenotypenetwork/SimGIC-twosides.cc

The similarity matrix between drugs and model organism phenotypes can also be viewed at http://phenomebrowser.net (e.g., for diclofenac: http://phenomebrowser.net/explore.php?val=STITCHORIG:000003032).