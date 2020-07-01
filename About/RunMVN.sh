#!/bin/bash 
POMXML_DIR="/c/Trading/Eclipse EE/Workspace/TradingAPI"
#POMXML_DIR="/c/Temp"
cd "$POMXML_DIR"
pwd

# Get the line with first occurrence of <version> as 
# <version>MAJOR.MINOR.BUILD</version> from pom.xml
function getFirstVersionLineFromPOM { 
	filename=pom.xml
    while read LINE; 
    do
    if [[ "$LINE" =~ "<version>" ]]; then 
        echo "$LINE"
        break
    fi
    done < $filename
}

# Get current version from raw vertion string as MAJOR.MINOR.BUILD 
function getCurrentVersion {
    # Sample: <version>1.0.5</version>"
    search1=">"
    search2="</"
    pos1=`awk -v a="$1" -v b="$search1" 'BEGIN{print index(a, b)}'`
    pos2=`awk -v a="$1" -v b="$search2" 'BEGIN{print index(a, b)}'`
    #echo $pos1  $pos2 # 19 26
    echo "$1" | cut -c $((pos1+1))-$((pos2-1)) 
}

# Get increamented version string as MAJOR.MINOR.BUILD+1 
function getIncreamentedVersion {
    MAJOR=$(echo $1 | awk '{split($0,a,"."); print a[1]}') 
    MINOR=$(echo $1 | awk '{split($0,a,"."); print a[2]}') 
    BUILD=$(echo $1 | awk '{split($0,a,"."); print a[3]}')

    increamented_version=`echo "$MAJOR.$MINOR.$((BUILD+1))"`
    echo $increamented_version 
}

getFirstVersionLineFromPOM 
raw_pom_version=$(getFirstVersionLineFromPOM) 
current_version=$(getCurrentVersion "$raw_pom_version")
increamented_version=$(getIncreamentedVersion "$current_version")

echo "Raw current version: $raw_pom_version"       
echo "My current version: $current_version"        
echo "Increamented version: $increamented_version" 

# Update pom.xml with new version
sed -i "0,/$current_version/{s/$current_version/$increamented_version/}" pom.xml 

mvn clean install