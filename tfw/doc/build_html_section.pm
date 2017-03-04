sub build_html_section () {

my ($category, $tgtdir) = @_;
my ($status, $line, $test_id, $test_title, $found, @requirements, $appraoch, @items, $item, $type, $last_type, $value, $req, $overall_test_status);
    $status = 'successful';
    $overall_test_status = 'unknown';

    $srcdir = './';


    if (open(IDENTIFICATION,"<TEST.$category.IDENTIFICATION") == 0) {
        print "Test Identification file can not be opened: TEST.$category.IDENTIFICATION\n";
        $status = 'fail';
    }

    if (open(TITLE, "<TEST.$category.MAJOR_SECTION_TITLE") == 0) {
        print "Can not open section title file: TEST.$category.MAJOR_SECTION_TITLE\n";
        $status = 'fail';
    }

    if (open(DESC, "<TEST.$category.MAJOR_SECTION_DESCRIPTION") == 0) {
        print "Can not open section description file: TEST.$category.MAJOR_SECTION_DESCRIPTION\n";
        $status = 'fail';
    }

    if (open(SUM,">${category}_toc.html") == 0) {
        print "Output file for section summary can not be opened: ${category}_toc.html\n";
        $status = 'fail';
    }

    print (SUM "<html>\n");
    print (SUM "<style>\n");
    print (SUM "body { background-color: white; }\n");
    print (SUM "body { font-family: sans-serif; }\n");
    print (SUM "li { font-size: 80%; }\n");
    print (SUM "</style>\n");
    print (SUM "<center>\n");
    $line = <TITLE>;
    close(TITLE);
    print (SUM "<h1>$line</h1>\n");
    print (SUM "</center>\n");
    print (SUM "<body>\n");
    $line = <DESC>;
    print (SUM "$line<hr>\n");
    print (SUM "<ol>\n");

    while (<IDENTIFICATION>) {
        chomp;
        $test_id    = $_;
        $test_title = <IDENTIFICATION>;
        chomp($test_title);


        if (open(OUT,">$tgtdir/$test_id.html") == 0) {
            print "Output file can not be opened: $tgtdir/$test_id.html\n";
            $status = 'fail';
        }

        print (OUT "<html>\n");

        print (OUT "<style>\n");
        print (OUT "body { background-color: white; }\n");
        print (OUT "body { font-family: sans-serif; }\n");
        print (OUT "li { font-size: 80%; }\n");
        print (OUT "</style>\n");

        print (OUT "<body>\n");

        print (OUT "<table BORDER WIDTH=\"90%\" NOSAVE >\n");

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nTest Case\n</td>\n");
        print (OUT "<td>\n");
        print (OUT "$test_id\n");
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nSystem Test\n</td>\n");
        print (OUT "<td>\n");
        print (OUT "PHS\n");
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nTitle\n</td>\n");
        print (OUT "<td>\n");
        print (OUT "$test_title\n");
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nObjective\n</td>\n");
        print (OUT "<td>\n");
        print (OUT "This test case is intended to verify the following implied or specified requirements:\n<br>\n" );

        if (open(APPROACH,"<TEST.$category.APPROACH") == 0) {
            print "Approach file can not be found for category: $category\n";
            $status = 'fail';
        }
        
        $found = 0;
        while (<APPROACH>) {
            $found |= ($_ =~ /$test_id/);
            if ($found) {
                chomp;
                @requirements = split(/,/,(split(/:/,$_))[1]);
                $approach = <APPROACH>;
                chomp($approach);
                last;
             } 
        }

        if (!$found) {
            print "Test ID was not found in approach file: $test_id\n";
            $status = 'fail';
        }

        print (OUT "<ul>\n");
        foreach $req (@requirements) {
            $req =~ s/ +//g;
            $req_file = (split(/-/,$req))[0];
            if (open(REQ,"<REQ.$req_file") == 0) {
                print "Requirement file could not be opened: $req_file\n";
                $status = 'fail';
            }
            while (<REQ>) {
                if ($_ =~ /^${req}$/) {
                    $_ = <REQ>;
                    chomp;
		    $_ =~ s/\\ldblquote/\'/g;
		    $_ =~ s/\\rdblquote/\'/g;
                    print (OUT '<li> '.$_."</li>\n");
                    last;
                } else {
		    <REQ>;
	        }
            }
        }

        print (OUT "</ul>\n");
        close(REQ);
        close(APPROACH);

        print (OUT "$approach<br>\n");
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        if (open(SETUP,"<TEST.$category.SETUP") == 0) {
            print "Setup file can not be found for category: $category\n";
            $status = 'fail';
        }

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nSetup\n</td>\n");
        print (OUT "<td>\n");

        $found = 0;
        while (<SETUP>) {
            $found |= ($_ =~ /$test_id/);
            if ($found) {
                print (OUT "<ul>\n");
	        $line = <SETUP>;
                chomp;
	        @items = split(/\/hr/,$line);
	        while($#items >= 0){
                    $item = shift(@items);
                    print (OUT "<li TYPE=CIRCLE> ".$item."</li>\n");
	        }
                print (OUT "</ul>\n");
                last;
	    }
        }
        close(SETUP);
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");


        if (open(PROCEDURES,"<TEST.$category.PROCEDURES") == 0) {
            print "Procedures file can not be found for category: $category\n";
            $status = 'fail';
        }

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nActions\n</td>\n");
        print (OUT "<td>\n");

        $found = 0;
        while (<PROCEDURES>) {
            $found |= ($_ =~ /$test_id/);
            if ($found) {
                print (OUT "<ol>\n");
	        $line = <PROCEDURES>;
                chomp;
	        @items = $line =~ m/(\/step|\/bullet) (.+?) (\\step|\\bullet)/g;
                $last_type = '/step';
	        $value = 1;
	        while($type = shift(@items)) {
		    if ($type eq '/step') {
		        if ($last_type eq '/bullet') {
                            print (OUT "</ul>\n");
                            print (OUT "<ol>\n");
		        }
	      	        $step = shift(@items);
                        print (OUT "<li VALUE=$value> ".$step."</li>\n");
		        $value++;
		    }
		    if ($type eq '/bullet') {
		        if ($last_type eq '/step') {
                            print (OUT "</ol>\n");
                            print (OUT "<ul>\n");
		        }
		        $step = shift(@items);
                        print (OUT '<li TYPE=SQUARE> '.$step."</li>\n");
		    }
		    $last_type = $type;
		    shift(@items);
	        }
                print (OUT "</ol>\n");
                last;
	    }
        }
        close(PROCEDURES);
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        if (!$found) {
            print "Test ID was not found in procedures file: $test_id\n";
            $status = 'fail';
        }

        if (open(EXPECTED_RESULTS,"<TEST.$category.EXPECTED_RESULTS") == 0) {
            print "Expected results file can not be found for category: $category\n";
            $status = 'fail';
        }

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nExpected\n</td>\n");
        print (OUT "<td>\n");

        $found = 0;
        while (<EXPECTED_RESULTS>) {
            $found |= ($_ =~ /$test_id/);
            if ($found) {
	        $line = <EXPECTED_RESULTS>;
                chomp;
	        @items = split(/\/hr/,$line);
	        while($#items >= 0){
                    $item = shift(@items);
                    print (OUT "$item<br>\n");
	        }
                last;
	    }
        }
        close(EXPECTED_RESULTS);
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        if (!$found) {
            print "Test ID was not found in expected results file: $test_id\n";
            $status = 'fail';
        }

        if (open(RESULTS,"<TEST.$category.RESULTS") == 0) {
            print "Results file can not be found for category: $category\n";
            $status = 'fail';
        }

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nResults\n</td>\n");
        print (OUT "<td>\n");

        $found = 0;
        while (<RESULTS>) {
            $found |= ($_ =~ /$test_id/);
            if ($found) {
	        $line = <RESULTS>;
                chomp($line);
                print (OUT "$line\n");
                last;
	    }
        }
        close(RESULTS);
        if ($line =~ /PASS/) {
            print (SUM "<li> <a style=\"color: #00F000\" href=$tgtdir/$test_id.html>$test_id - $test_title</a></ul></li>\n");
            if ($overall_test_status eq 'unknown') {
                $overall_test_status = 'pass';
	    }
        } elsif ($line =~ /FAIL/) {
            print (SUM "<li> <a style=\"color: #FF0000\" href=$tgtdir/$test_id.html>$test_id - $test_title</a></ul></li>\n");
            $overall_test_status = 'fail';
        } else {
            print (SUM "<li> <a href=$tgtdir/$test_id.html>$test_id - $test_title</a></ul></li>\n");  
        }

        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        if (!$found) {
            print "Test ID was not found in results file: $test_id\n";
            $status = 'fail';
        }


        if (open(CLEANUP,"<TEST.$category.CLEANUP") == 0) {
            print "Results file can not be found for category: $category\n";
            $status = 'fail';
        }

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nCleanup\n</td>\n");
        print (OUT "<td>\n");

        $found = 0;
        while (<CLEANUP>) {
            $found |= ($_ =~ /$test_id/);
            if ($found) {
	        $line = <CLEANUP>;
                chomp($line);
                print (OUT "$line\n");
                last;
	     }
        }
        close(CLEANUP);
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        if (!$found) {
            print "Test ID was not found in cleanup file: $test_id\n";
            $status = 0;
        }

        print (OUT "<tr NOSAVE>\n");
        print (OUT "<td>\nRequirements\n</td>\n");
        print (OUT "<td>\n");

        $line = '';
        foreach $req (@requirements) {
            if ($line eq '') {
                $line = $req
	    } else {
	        $line .= ', ' . $req;
	    }
        }
        print (OUT "$line\n");
        print (OUT "</td>\n</tr>\n");
        print (OUT "</tr>\n");

        print (OUT "</table>\n");
        print (OUT "</body>\n");
        print (OUT "</html>\n");
        close (OUT);
#        if ($status eq 'fail') {
#            last;
#	}
    }
    close(IDENTIFICATION);
    print (SUM "</ol>\n");
    print (SUM "</body>\n");
    print (SUM "</html>\n");
    close (SUM);
    return ($status, $overall_test_status);
}
1
