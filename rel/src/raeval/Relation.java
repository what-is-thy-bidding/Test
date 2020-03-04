/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package raeval;


import java.util.*;
import java.io.*;


/**
 *
 * @author nickeveritt
 */


//========================================================================
// Helper class for maintaining attributes of a relation
//========================================================================

class RelationAttribute {

    // instance variables

    String attrName;
    String attrDomain;
    
    // constructor to initalise instance vars
    public RelationAttribute(String name, String domain) {
        // initialise the instance vars
        this.attrName = name;
        this.attrDomain = domain;
    }

    // getter for attribute name
    public String getName() {
        return this.attrName;
    }
    
    // setter for attribute name
    public void setName(String name) {
        this.attrName = name;
    }

    // getter for domain name
    public String getDomain() {
        return this.attrDomain;
    }
    
}




//========================================================================
// Helper class to represent a tuple
//========================================================================

class RelationTuple {

    Map<String,String> tuple;
    Relation relevantRelation;

    // constructor for tuple for relation rel
    public RelationTuple(Relation rel) {
        // initialise instance vars
        this.tuple = new HashMap<String,String>();
        this.relevantRelation = rel;
    }

    // create a deep copy
    public RelationTuple clone() {
        RelationTuple cloneTuple = new RelationTuple(this.relevantRelation);
        cloneTuple.tuple = new HashMap(this.tuple);
        return cloneTuple;
    }

    // determine whether the argument tuple matches this tuple
    public boolean matches(RelationTuple compTuple) {
        boolean doesMatch = true;
        // check to see if attribute set matches between the two
        Iterator iter = this.tuple.keySet().iterator();
        while (iter.hasNext() && doesMatch) {
            String attrName = (String)iter.next();
            if (!compTuple.attributeUsed(attrName)) {
                doesMatch = false;
            } else {
                if (!compTuple.getValue(attrName).matches(this.tuple.get(attrName))) doesMatch = false;
            }
        }
        // return the result
        doesMatch = this.isDuplicate(compTuple);
        return doesMatch;
    }

    // set a value in the tuple for a specified attribute
    public void setValue(String forAttr, String toValue) {
        tuple.put(forAttr, toValue);
    }

    // get a value for a specified attribute
    public String getValue(String forAttr) {
        return tuple.get(forAttr);
    }

    // remove a value for a specified attribute
    // only valid when deleting the attribute from the header as well
    public void removeValue(String forAttr) {
        tuple.remove(forAttr);
    }

    // check whether an attribute name has been used
    public boolean attributeUsed(String attr) {
        return this.tuple.containsKey(attr);
    }

    // compare attribute names to determine whether a tuple is a duplicate
    // only valid when both tuples are from the same relation
    public boolean isDuplicate(RelationTuple otherTuple) {
        // iterate through values until find a point of difference
        boolean duplicate = true;
        Iterator iter = this.tuple.keySet().iterator();
        while (iter.hasNext() && duplicate) {
            String attrName = (String)iter.next();
            String localValue = this.tuple.get(attrName);
            String otherValue = otherTuple.getValue(attrName);
            // check for a non match between the values
            if (!localValue.matches(otherValue)) duplicate = false;
        }
        return duplicate;
    }

    // convert a tuple to a string - for debugging purposes
    public String toString() {
        String res = "<";
        Iterator iter = this.tuple.keySet().iterator();
        while (iter.hasNext()) {
            String attr = (String)iter.next();
            res = res.concat(attr);
            res = res.concat(":");
            String value = this.tuple.get(attr);
            res = res.concat(value);
            // only print a space if there is a next attribute
            if (iter.hasNext()) res = res.concat(", ");
        }
        res = res.concat(">");
        return res;
    }

    // print this tuple to the standard output
    public void print() {
        System.out.print(this.toString());
    }
}




//========================================================================
// Class Relation
//========================================================================

public class Relation {

    // class variables
    static HashMap<String,Relation> definedRels;
    static int                      resultCounter;

    // instance variables
    ArrayList<RelationAttribute>    attributes;
    ArrayList<RelationTuple>        tuples;
    
    // error handling
    static boolean                  errorCondition;
    static int                      errorType;
    static String                   errorMessage;


    //========================================================================
    // Constructor and clone
    //========================================================================

    public Relation() {
        // initialise the instance vars
        this.attributes = new ArrayList<RelationAttribute>();
        this.tuples     = new ArrayList<RelationTuple>();
        if (definedRels == null) initDefinedRels();
        errorCondition  = false;
    }

    /**
     * Creates a deep copy of this relation.
     * @return new Relation object identical to receiver
     */
    public Relation clone() {
        Relation cloneRelation = new Relation();
        // iterate through cloning each attribute
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute thisAttr = (RelationAttribute)iter.next();
            cloneRelation.addAttribute(thisAttr.getName(), thisAttr.getDomain());
        }
        // iterate through cloning each tuple
        iter = this.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple thisTuple = (RelationTuple)iter.next();
            RelationTuple cloneTuple = thisTuple.clone();
            cloneRelation.addTuple(cloneTuple);
        }
        // and return the result
        return cloneRelation;
    }



    //========================================================================
    // Manage static library of defined relations
    //========================================================================

    static void initDefinedRels() {
        definedRels = new HashMap<String,Relation>();
        resultCounter = 0;
    }

    // get the next place holder name
    static String definedNextName() {
        resultCounter++;
        String name = "INTERMEDIATE".concat(String.valueOf(resultCounter));
        return name;
    }

    // check that whether a relation with a given name exists
    static boolean definedNameExists(String name) {
        if (definedRels.keySet().contains(name)) return true;
        return false;
    }

    // return reference to a named relation
    static Relation definedRelation(String name) {
        if (!definedNameExists(name)) return null;
        return definedRels.get(name);
    }

    // add a relation to the library
    static void definedAssign(String name, Relation reln) {
        if (!definedNameExists(name)) {
            definedRels.put(name, reln);
        }
    }

    public static void referencesInit() {
        initDefinedRels();
    }

    /**
     * Store a relation instance in the static environment
     * @param name the name to be given to the stored relation
     * @param reln the relation instance
     */
    public static void assign(String name, Relation reln) {
        definedAssign(name,reln);
    }

    /**
     * Store this instance in the static environment
     * @param name the name to be given to the relation
     */
    public void assign(String name) {
        definedAssign(name,this);
    }

    /**
     * Return a reference to a relation with a given name in
     * the static environment
     * @param name the name of the relation
     * @return reference to the named relation, returns null if the name
     * does not exist
     */
    public static Relation referenceForRelation(String name) {
        if (!definedNameExists(name)) return null;
        return definedRelation(name);
    }
    
    // remove a relation from the library (non-destructive)
    static void definedRemove(String name) {
        if (definedNameExists(name)) {
            definedRels.remove(name);
        }
    }

    // rename a named relation
    static void definedRename(String wasName, String toName) {
        if (definedNameExists(wasName) && !definedNameExists(toName)) {
            Relation tempReln = definedRelation(wasName);
            definedAssign(toName,tempReln);
            definedRemove(wasName);
        }
    }

    // carry out garbage collection
    static void definedGarbageCollect() {
        Iterator iter = definedRels.keySet().iterator();
        while (iter.hasNext()) {
            String relName = (String)iter.next();
            if (relName.startsWith("INTERMEDIATE")) iter.remove();
        }
    }


    //========================================================================
    // General properties of relations
    //========================================================================

    /**
     * Returns a string representation of the heading of the relation.
     * The format is
     * (A1:D1, A2:D2, ... , An:Dn)
     * Where:
     * Ai is the attribute name of the i-th attribute,
     * Di is the domain name of the i-th attribute,
     * and n is the degree of the relation
     * @return string representation of the relational heading
     */
    public String heading() {
        // initialise the result string bracket
        String result = "(";
        // iterate through each attribute
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            // add this attribute into the result string
            RelationAttribute element = (RelationAttribute)iter.next();
            result = result.concat(element.getName());
            result = result.concat(":");
            result = result.concat(element.getDomain());
            // only add a comma if there is a next element
            if (iter.hasNext()) result = result.concat(", ");
        }
        // close off the result string bracket
        result = result.concat(")");
        return result;
    }

    /**
     * Returns the cardinality of the relation - that is the number of tuples
     * within the body.
     * @return  the number of tuples within the body of the relation
     */
    public int cardinality() {
        // equivalent to size of the list of tuples
        return tuples.size();
    }

    /**
     * Returns the degree of the relation - that is the number of attributes
     * in the heading.
     * @return  the number of attributes within the heading of the relation
     */
    public int degree() {
        // equivalent to the size of the list of attributes
        return attributes.size();
    }



    //========================================================================
    // Attributes
    //========================================================================

    // Check to see if an attribute with this name already exists
    boolean attributeNameExists(String checkName) {
        // iterate through each attribute
        // return true if a matching name is found
        // otherwise return false once all names have been checked
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute element = (RelationAttribute)iter.next();
            if (checkName.matches(element.getName())) return true;
        }
        return false;
    }
    
    // Return the domain for a given attribute name
    String domainForAttribute(String attrName) {
        // iterate through each attribute until attrName is found
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute element = (RelationAttribute)iter.next();
            if (attrName.matches(element.getName())) {
                // found the attribute - return the domain
                return element.getDomain();
            }
        }
        // if the attribute was not found, then return null pointer
        return null;
    }
    
    public String attributeName(int index) {
        if (index >= 0 && index < this.degree()) {
            RelationAttribute attr = (RelationAttribute)this.attributes.get(index);
            return attr.getName();
        }
        return null;
    }

    /**
     * Adds a new attribute to the relational heading of the relation
     * @param attrName      specifies the name of the new attribute,
     * names cannot be reused in the same relation
     * @param domainName    specified the domain of the new attribute
     * @return              zero if successful, otherwise an error code
     */
    public void addAttribute(String attrName, String domainName) {
        // cannot add an attribute that already exists
        // also cannot add an attribute if there are tuples in the relation
        // this would create unfilled values
        if (!attributeNameExists(attrName) && this.cardinality() == 0) {
            // otherwise continue by creating this attribute and adding
            RelationAttribute newAttribute =
                    new RelationAttribute(attrName,domainName);
            attributes.add(newAttribute);
        }   
    }

    /**
     * Renames an attribute in the relational heading
     * @param fromName the name of the attribute to be renamed
     * @param toName the new name for the attribute
     * @return zero if successful, otherwise an error code
     */
    public void renameAttribute(String fromName, String toName) {
        // ensure that the fromName already exists and the to name does not
        if (attributeNameExists(fromName) && !attributeNameExists(toName)) {
            // carry out the renaming
            // iterate through the attributes until match is found and changed
            Iterator iter = this.attributes.iterator();
            boolean found = false;
            while (iter.hasNext() && !found) {
                RelationAttribute element = (RelationAttribute)iter.next();
                if (fromName.matches(element.getName())) {
                    // found attribute so change the name
                    element.setName(toName);
                    // success
                    found = true;
                }
            }
            // iterate though the tuples changing the relevant attribute name
            iter = this.tuples.iterator();
            while (iter.hasNext()) {
                RelationTuple tuple = (RelationTuple)iter.next();
                // get the value for the old attribute name
                String value = tuple.getValue(fromName);
                // remove the old attribute name
                tuple.removeValue(fromName);
                // add under the new name
                tuple.setValue(toName, value);
            }
        }
    }

    /**
     * Remove an attribute from the relation.  Also remove all values
     * associated with that attribute from tuples in the body of the
     * relation.
     * @param name the name of the attribute to be removed
     * @return zero if successful, otherwise error code
     */
    public void removeAttribute(String name) {
        // check that attribute exists
        if (attributeNameExists(name)) {
            // remove the attribute from the header
            Iterator iter = this.attributes.iterator();
            while (iter.hasNext()) {
                RelationAttribute element = (RelationAttribute)iter.next();
                if (name.matches(element.getName())) {
                    // this name matches, so remove
                    iter.remove();
                }
            }
            // now remove values for this attribute from all tuples
            iter = this.tuples.iterator();
            while (iter.hasNext()) {
                RelationTuple tuple = (RelationTuple)iter.next();
                tuple.removeValue(name);
            }
        }
    }



    //========================================================================
    // Tuples
    //========================================================================

    // add a tuple into the collection
    void addTuple(RelationTuple newTuple) {
        tuples.add(newTuple);
    }

    // find and remove any tuples that have become duplicated (relationize!)
    void removeDuplicateTuples() {
        // iterate through each tuple
        Iterator iter = this.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple tuple = (RelationTuple)iter.next();
            // compare with all other tuples
            boolean duplicate = false;
            Iterator other = this.tuples.iterator();
            while (other.hasNext() && !duplicate) {
                RelationTuple otherTuple = (RelationTuple)other.next();
                // make sure not comparing self with self
                if (tuple != otherTuple) duplicate = tuple.isDuplicate(otherTuple);
            }
            // if is a duplicate then remove
            if (duplicate) iter.remove();
        }
    }

    // determine whether there is a match for this tuple
    boolean containsTuple(RelationTuple tuple) {
        // iterate through each tuple
        Iterator iter = this.tuples.iterator();
        boolean matched = false;
        while (iter.hasNext() && !matched) {
            RelationTuple compareTuple = (RelationTuple)iter.next();
            // compare with reference tuple
            Iterator attrIter = this.attributes.iterator();
            while (attrIter.hasNext()) {
                RelationAttribute attr = (RelationAttribute)attrIter.next();
                matched = true;
                if (tuple.attributeUsed(attr.getName())) {
                    // the attribute is in the tuple
                    String tupleValue = tuple.getValue(attr.getName());
                    String compareValue = compareTuple.getValue(attr.getName());
                    if (!tupleValue.matches(compareValue)) matched = false;
                } else {
                    // attribute is not in the tuple, so cannot be a match
                    matched = false;
                }
            }
        }
        // return result
        return matched;
    }

    public String valueForTuple(int index, String attr) {
        if (index >= 0 && index < this.cardinality() && this.attributeNameExists(attr)) {
            RelationTuple tuple = this.tuples.get(index);
            return tuple.getValue(attr);
        }
        return null;
    }
    
    /**
     * Adds a tuple parsed from a string into the body of the relation.
     * @param tupleValues contains a comma-separated list of values of the
     * form Attr:Value where Attr is an attribute name that exists in the
     * relation and Value is the value for that attribute in this tuple.
     * Optionally the list may be enclosed by angle brackets.
     * @return zero if successful otherwise returns an error code
     */
    public void parseTuple(String tupleValues) {
        // strip off the optional enclosing angle brackets if present
        if (tupleValues.startsWith("<")) tupleValues = tupleValues.substring(1);
        if (tupleValues.endsWith(">")) tupleValues = tupleValues.substring(0, tupleValues.length()-1);
        // create a new tuple
        RelationTuple tuple = new RelationTuple(this);
        // split the string into values separated by commas
        String[] values = tupleValues.split("\\,");
        // take each value in turn
        for (int i=0; i<values.length; i++) {
            // split into a pair of values based on the colon
            String[] pair = values[i].split("\\:");
            // must be two in a pair otherwise malformed
            if (pair.length == 2) {
                // get rid of trailing and ending whitespace
                pair[0] = pair[0].trim();
                pair[1] = pair[1].trim();
                // now pair[0] is the attribute name and pair[1] the value
                // ensure that the attribute name exists
                if (!attributeNameExists(pair[0])) {
                    // do not allow the same attribute name to be
                    // used twice in this tuple
                    if (tuple.attributeUsed(pair[0])) {
                        // ok so add it into the tuple
                        tuple.setValue(pair[0], pair[1]);  
                    }
                }

            }
        }
        // check that all attribute names have been used
        boolean allUsed = true;
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute attr = (RelationAttribute)iter.next();
            if (!tuple.attributeUsed(attr.getName())) allUsed = false;
        }
        // this tuple has parsed ok, so add it
        if (allUsed) this.addTuple(tuple);
    }

    /**
     * Prints all tuples to the standard output.
     */
    public void printTuples() {
        Iterator iter = this.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple tuple = (RelationTuple)iter.next();
            tuple.print();
        }
    }



    //========================================================================
    // Relational operator: project
    //========================================================================

    /**
     * Returns a new relation that projects the given attributes
     * @param overAttributes comma separated list of attributes to be projected
     * @return new relation containing only the projected attributes
     */
    public Relation project(String overAttributes) {
        // split the over list by comma
        String[] overAttrs = overAttributes.split("\\,");
        // check that each over attribute is in this relation and trim whitespace
        for (int i=0; i<overAttrs.length; i++) {
            overAttrs[i] = overAttrs[i].trim();
            if (overAttrs[i].length() > 0) if (!attributeNameExists(overAttrs[i])) return null;
        }
        // clone the current relation
        Relation projected = this.clone();
        // iterate over the  relation, remove attributes not in the over list
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            // get the name of each attribute
            RelationAttribute attr = (RelationAttribute)iter.next();
            String attrName = attr.getName();
            // compare the name with the list to be projected
            boolean found = false;
            for (int i=0; i<overAttrs.length; i++) {
                if (overAttrs[i].length() > 0) if (attrName.matches(overAttrs[i])) found = true;
            }
            // if it is not found then delete the attribute from the projection
            if (!found) projected.removeAttribute(attrName);
        }
        // remove duplicate tuples
        projected.removeDuplicateTuples();
        // operation complete
        return projected;
    }



    //========================================================================
    // Relational operator: join
    //========================================================================

    /**
     * Returns a new relation representing a natural join between the
     * receiver and the relation given as a parameter.  The natural
     * join contains tuples that match on their common attribute names.
     * @param joinWith
     * @return
     */
    public Relation join(Relation joinWith) {
        // instantiate a new relation object
        Relation joined = new Relation();
        // determine the common attributes
        ArrayList<String> commonAttr = new ArrayList<String>();
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute thisAttr = (RelationAttribute)iter.next();
            if (joinWith.attributeNameExists(thisAttr.getName())) {
                // this is a common attribute if over the same domain
                String localDomain = thisAttr.getDomain();
                String joinDomain = joinWith.domainForAttribute(thisAttr.getName());
                if (localDomain.matches(joinDomain)) {
                    // name and domain both match
                    commonAttr.add(thisAttr.getName());
                }
            }
        }
        // add all attributes from the local relation
        // and create the localUnshared list
        ArrayList<String> localUnshared = new ArrayList<String>();
        iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute thisAttr = (RelationAttribute)iter.next();
            String attrName = thisAttr.getName();
            joined.addAttribute(attrName, thisAttr.getDomain());
            // unshared if not in the list of common attributes
            if (!commonAttr.contains(attrName)) localUnshared.add(attrName);
        }
        // now add the unshared attributes from the join relation
        ArrayList<String> joinUnshared = new ArrayList<String>();
        iter = joinWith.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute joinAttr = (RelationAttribute)iter.next();
            String attrName = joinAttr.getName();
            // unshared if not in the list of common attributes
            if (!commonAttr.contains(attrName)) {
                joined.addAttribute(attrName, joinAttr.getDomain());
                joinUnshared.add(attrName);
            }
        }
        // iterate over each tuple in this relation
        iter = this.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple thisTuple = (RelationTuple)iter.next();
            thisTuple.print();
            // iterate over each tuple in the join relation
            Iterator joinIter = joinWith.tuples.iterator();
            while (joinIter.hasNext()) {
                RelationTuple joinTuple = (RelationTuple)joinIter.next();
                // determine whether matches on common attributes
                Iterator commIter = commonAttr.iterator();
                boolean commonMatch = true;
                while (commIter.hasNext() && commonMatch) {
                    String commAttr = (String)commIter.next();
                    String localValue = thisTuple.getValue(commAttr);
                    String joinValue = joinTuple.getValue(commAttr);
                    if (!localValue.matches(joinValue)) commonMatch = false;
                }
                if (commonMatch) {
                    // this local and join tuple does match on common attributes
                    // build a new tuple
                    RelationTuple buildTuple = new RelationTuple(joined);
                    // add the common attributes
                    Iterator addIter = commonAttr.iterator();
                    while (addIter.hasNext()) {
                        String attrName = (String)addIter.next();
                        buildTuple.setValue(attrName, thisTuple.getValue(attrName));
                    }
                    // add the local unshared attributes
                    addIter = localUnshared.iterator();
                    while (addIter.hasNext()) {
                        String attrName = (String)addIter.next();
                        buildTuple.setValue(attrName, thisTuple.getValue(attrName));
                    }
                    // add the join unshared attributes
                    addIter = joinUnshared.iterator();
                    while (addIter.hasNext()) {
                        String attrName = (String)addIter.next();
                        buildTuple.setValue(attrName, joinTuple.getValue(attrName));
                    }
                    // add the tuple to the new joined relation
                    joined.addTuple(buildTuple);
                }
            }
        }
        // tidy up
        joined.removeDuplicateTuples();
        // operation complete
        return joined;
    }



    //========================================================================
    // Relational operator: select
    //========================================================================

    // evaluate select based on a postfix stack
    Relation selectPostfixStack(Stack<String> postfixStack) {
        // start with a copy of the current relation
        Relation selected = this.clone();
        // iterate over each tuple in the new relation
        Iterator iter = selected.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple tuple = (RelationTuple)iter.next();
            // evalute each tuple using the postfix stack
            Stack<String> cloneStack = (Stack<String>)postfixStack.clone();
            String result = evaluateExpression(cloneStack,tuple);
            // the result determines inclusion (i.e. survival) in the new relation
            if (!result.matches("true")) iter.remove();
        }
        // operation complete
        selected.removeDuplicateTuples();
        return selected;
    }

    /**
     * Returns a new relation obtained by selecting tuples from the receiver
     * in accordance with the criteria specified in the where parameter.
     * @param where criteria for selection for new relation
     * @return a new relation containing tuples that meet the criteria
     */
    public Relation select(String where) {
        // build a postfix notation queue of the where clause
        Stack<String> postfixStack = shuntingYardAlgorithm(where);
        postfixStack = stackReverse(postfixStack);
        return selectPostfixStack(postfixStack);
    }

    //========================================================================
    // Relational operator: divide
    //========================================================================

    /**
     * Returns a new relation which is the relational division of the
     * receiver by the divideBy parameter.  Only those attributes (and
     * values) that are not in the divideBy relation are included in the
     * new relation.
     * @param divideBy the divisor relation
     * @return a new relation containing only attributes not in the divisor
     */
    public Relation divide(Relation divideBy) {
        // instantiate a new relation
        Relation r = this.clone();
        // list attribute names unique to r
        ArrayList<String> uniqToR = new ArrayList<String>();
        Iterator iter = r.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute attr = (RelationAttribute)iter.next();
            boolean unique = true;
            // not unique if name in both relations
            if (divideBy.attributeNameExists(attr.getName())) {
                unique = false;
            }
            // it is unique so add to the list
            if (unique) uniqToR.add(attr.getName());
        }
        // convert the list of unique attributes into a string
        iter = uniqToR.iterator();
        String uniqStr = "";
        while (iter.hasNext()) {
            String attr = (String)iter.next();
            uniqStr = uniqStr + attr;
            if (iter.hasNext()) uniqStr = uniqStr + ", ";
        }
        Relation w = new Relation();
        // now form t by project r over the uniq attributes
        // and taking the cartesian product with the divisor
        Relation t = r.project(uniqStr);
        System.out.println("t is: \n");
        t.printTuples();
        t = t.times(divideBy);
        // now subtract r
        Relation u = t.difference(r);
        // and take the projection over the unique attributes
        Relation v = u.project(uniqStr);
        // now reproject r on its unique attributes and subtract v
        w = r.project(uniqStr);
        w = w.difference(v);
        // operation complete
        w.removeDuplicateTuples();
        return w;
    }



    //========================================================================
    // Relational operator: union
    //========================================================================

    // check whether this relation is union compatible with another
    // ... they must both share the same attribute names and domains
    boolean unionCompatible(Relation withRelation) {
        // quick check: must be of same degree or cannot be compatible
        if (this.degree() != withRelation.degree()) return false;
        boolean isCompatible = true;
        // iterate over attributes in this relation
        // this is like for like because we know both have same degree
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext() && isCompatible) {
            RelationAttribute thisAttr = (RelationAttribute)iter.next();
            // fail if this attribute does not exist in the other relation
            if (withRelation.attributeNameExists(thisAttr.getName())) {
                // attribute with this name exists, go on to check domain
                if (!thisAttr.getDomain().matches(withRelation.domainForAttribute(thisAttr.getName()))) {
                    // domains do not match = fail
                    isCompatible = false;
                }
            } else {
                // no attribute with this name = fail
                isCompatible = false;
            }
        }
        // return the result
        return isCompatible;
    }

    /**
     * Returns a new relation containing tuples of both the receiver and the
     * parameter relation.
     * @param withRelation the relation with which to perform the union
     * @return new relation
     */
    public Relation union(Relation withRelation) {
        // first check for union compatibility
        if (!unionCompatible(withRelation)) {
            setError(2,"relations for union are not union compatible");
            return null;
        }
        // clone current relation
        Relation unionRel = this.clone();
        // now add all tuples from contributing relation
        Iterator iter = withRelation.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple tuple = (RelationTuple)iter.next();
            unionRel.addTuple(tuple);
        }
        // tidy up duplicates
        unionRel.removeDuplicateTuples();
        // operation complete
        return unionRel;
    }



    //========================================================================
    // Relational operator: intersection
    //========================================================================

    /**
     * Returns a new relation containing those tuples that exist in both
     * the receiver and the parameter relation
     * @param withRelation the relation with which to perform the intersection
     * @return new relation
     */
    public Relation intersection(Relation withRelation) {
        // first check for union compatibility
        if (!unionCompatible(withRelation)) {
            setError(2,"relations for intersection are not union compatible");
            return null;
        }
        // clone current relation
        Relation interRel = this.clone();
        // check all tuples for membership in the intersected relation
        Iterator iter = interRel.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple tuple = (RelationTuple)iter.next();
            if (!withRelation.containsTuple(tuple)) {
                // tuple is not in both relations - remove
                iter.remove();
            }
        }
        // operation complete
        interRel.removeDuplicateTuples();
        return interRel;
    }



    //========================================================================
    // Relational operator: difference
    //========================================================================

    /**
     * Returns a new relation containing those tuples in the receiver that are
     * not in the parameter relation
     * @param withRelation the relation to subtract
     * @return new relation
     */
    public Relation difference(Relation withRelation) {
        // first check for union compatibility
        if (!unionCompatible(withRelation)) {
            setError(2,"relations for difference are not union compatible");
            return null;
        }
        // clone current relation
        Relation diffRel = this.clone();
        // check all tuples for membership in the difference relation
        Iterator iter = diffRel.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple resTuple = (RelationTuple)iter.next();
            boolean inThis = false;
            Iterator withIter = withRelation.tuples.iterator();
            while (withIter.hasNext() && !inThis) {
                RelationTuple withTuple = (RelationTuple)withIter.next();
                if (withTuple.matches(resTuple)) {
                    inThis = true;
                }
            }
            if (inThis) iter.remove();
        }
        // operation complete
        diffRel.removeDuplicateTuples();
        return diffRel;
    }



    //========================================================================
    // Relational operator: times
    //========================================================================

    /**
     * Returns a new relation giving the cartesian product of the receiver
     * and the parameter relation
     * @param timesBy the parameter relation
     * @return new relation
     */
    public Relation times(Relation timesBy) {
        // instantiate a new relation
        Relation timesRel = new Relation();
        // add attributes from the current relation to the new relation
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute attr = (RelationAttribute)iter.next();
            timesRel.addAttribute(attr.getName(),attr.getDomain());
        }
        // add attributes from the times relation to the new relation
        iter = timesBy.attributes.iterator();
        while (iter.hasNext()) {
            RelationAttribute attr = (RelationAttribute)iter.next();
            timesRel.addAttribute(attr.getName(),attr.getDomain());
        }
        // iterate through each tuple in the relation
        iter = this.tuples.iterator();
        while (iter.hasNext()) {
            RelationTuple tuple = (RelationTuple)iter.next();
            // and iterate through each tuple in the times by relation
            Iterator timesIter = timesBy.tuples.iterator();
            while (timesIter.hasNext()) {
                RelationTuple timesTuple = (RelationTuple)timesIter.next();
                // create a new tuple
                RelationTuple newTuple = new RelationTuple(timesRel);
                // add attributes from the base tuple
                Iterator attrIter = this.attributes.iterator();
                while (attrIter.hasNext()) {
                    RelationAttribute attr = (RelationAttribute)attrIter.next();
                    newTuple.setValue(attr.getName(), tuple.getValue(attr.getName()));
                }
                // add attributes from the times tuple
                attrIter = timesBy.attributes.iterator();
                while (attrIter.hasNext()) {
                    RelationAttribute attr = (RelationAttribute)attrIter.next();
                    newTuple.setValue(attr.getName(), timesTuple.getValue(attr.getName()));
                }
                // add tuple to new relationship
                timesRel.addTuple(newTuple);
            }
        }
        // tidy
        timesRel.removeDuplicateTuples();
        // operation complete
        return timesRel;
    }



    //========================================================================
    // Expression parsing and evaluation
    //========================================================================

    // tokenise an expresssion
    ArrayList<String> tokenise(String expression) {
        // initialiase
        ArrayList<String> tokens = new ArrayList<String>();
        String buildToken = "";
        String lastToken = "";
        boolean firstToken = true;
        boolean insideQuotes = false;
        // remove whitespace
        expression = expression.replaceAll("\\s+"," ");
        // iterate over each character in the expression
        for (int i=0; i<expression.length(); i++) {
            char ch = expression.charAt(i);
            char chPeek = 0;
            int j = i + 1;
            while (j<expression.length() && chPeek == 0) {
                if (expression.charAt(j) > 32) chPeek = expression.charAt(j);
                j++;
            }  
            boolean add = true;
            boolean split = false;
            // handle quotes
            if (ch == 34) {
                split = true;
                add = true;
                insideQuotes = !insideQuotes;
            }
            // now check the ch
            if (ch == ' ') {
                split = true;
                add = false;
            }
            boolean symbol = false;
            // treat following as immediate splits that are symbols (tokens in their own right)
            if (ch == '+' || ch == '*' || ch == '/' || ch == '(' || ch == ')') {
                split = true;
                symbol = true;
            }
            // also treat commas as above
            if (ch == ',') {
                split = true;
                symbol = true;
            }
            // handle the - symbol
            if (ch == '-') {
                // assume this is unary negation
                boolean unaryNegation = false;
                String compareToken = lastToken;
                if (buildToken.length() > 0) compareToken = buildToken;
                // but not after an operand or after a right bracket
                if (tokenIsOperator(compareToken)) unaryNegation = true;
                if (tokenIsLeftParenthesis(compareToken)) unaryNegation = true;
                // and if it is the first token then it must be unary negation
                if (firstToken) unaryNegation = true;
                // handle unary negation or subtraction
                if (unaryNegation) {
                    // if unary negation then change the character to _ and split
                    split = true;
                    symbol = true;
                    ch = '_';
                } else {
                    // if subtraction then split
                    split = true;
                    symbol = true;
                }
            }
            // treat following as immediate splits, but only tokens in own right if not start of <= or >= or <>
            if (ch == '<' || ch == '>') {
                split = true;
                if (chPeek != '=' && chPeek != '>') symbol = true;
            }
            // treat as immediate split, but keep := in its on right
            if (ch == ':') {
                split = true;
                symbol = true;
                if (chPeek == '=') symbol = false;
            }
            if (ch == '=') {
                split = true;
                symbol = true;
            }
            // treat following as immediate splits
            if (buildToken.matches(":") && ch == '=') {
                split = true;
                buildToken = ":=";
                add = false;
                symbol = false;
            }
            if (buildToken.matches("<") && ch == '=') {
                split = true;
                buildToken = "<=";
                add = false;
                symbol = false;
            }
            if (buildToken.matches(">") && ch == '=') {
                split = true;
                buildToken = ">=";
                add = false;
                symbol = false;
            }
            if (buildToken.matches("<") && ch == '>') {
                split = true;
                buildToken = "<>";
                add = false;
                symbol = false;
            }
            // carry out a split if required
            if (split && !insideQuotes) {
                if (buildToken.length() > 0) {
                    // cater for final quotes
                    if (ch == 34 && !insideQuotes) {
                        ch = 0;
                        buildToken = buildToken.concat("\"");
                    }
                    // only add non-null strings
                    buildToken = buildToken.trim();
                    tokens.add(buildToken);
                    lastToken = buildToken;
                    firstToken = false;
                }
                buildToken = "";
                if (symbol) {
                    // if this is a symbol then create a token for it and add as well
                    String theSymbol = String.valueOf(ch);
                    // special symbol for commas
                    if (ch == ',') theSymbol = "comma";
                    tokens.add(theSymbol);
                    lastToken = theSymbol;
                    add = false;
                    firstToken = false;
                }
            }
            // add characters into the building token
            if ((add && ch != 0) || insideQuotes) {
                buildToken = buildToken.concat(String.valueOf(ch));
            }
        }
        // add the last token
        if (buildToken.length() > 0) tokens.add(buildToken);
        // return the tokens
        return tokens;
    }

    // determine whether a token is an operator
    boolean tokenIsOperator(String token) {
        boolean isOperator = false;
        //if (token.matches("select")) isOperator = true;
        if (token.matches("load")) isOperator = true;
        if (token.matches("show")) isOperator = true;
        if (token.matches("where")) isOperator = true;
        if (token.matches("over")) isOperator = true;
        if (token.matches("join")) isOperator = true;
        if (token.matches("by")) isOperator = true;
        if (token.matches("times")) isOperator = true;
        if (token.matches("union")) isOperator = true;
        if (token.matches("intersection")) isOperator = true;
        if (token.matches("difference")) isOperator = true;
        if (token.matches("rename")) isOperator = true;
        if (token.matches(":=")) isOperator = true;
        if (token.matches("alias")) isOperator = true;
        if (token.matches("_")) isOperator = true;
        if (token.matches("\\+")) isOperator = true;
        if (token.matches("-")) isOperator = true;
        if (token.matches("\\*")) isOperator = true;
        if (token.matches("/")) isOperator = true;
        if (token.matches("and")) isOperator = true;
        if (token.matches("or")) isOperator = true;
        if (token.matches("not")) isOperator = true;
        if (token.matches("<")) isOperator = true;
        if (token.matches("<=")) isOperator = true;
        if (token.matches(">")) isOperator = true;
        if (token.matches(">=")) isOperator = true;
        if (token.matches("=")) isOperator = true;
        if (token.matches("<>")) isOperator = true;
        return isOperator;
    }

    // given that a token is an operator, determine if left associative
    boolean tokenOperatorIsLeftAssociative(String token) {
        boolean isLeftAssociative = false;
        // unary negation _ is right associative so leave false
        //if (token.matches("select")) isLeftAssociative = true;
        if (token.matches("load")) isLeftAssociative = true;
        if (token.matches("show")) isLeftAssociative = true;
        if (token.matches("where")) isLeftAssociative = true;
        if (token.matches("over")) isLeftAssociative = true;
        if (token.matches("join")) isLeftAssociative = true;
        if (token.matches("by")) isLeftAssociative = true;
        if (token.matches("times")) isLeftAssociative = true;
        if (token.matches("union")) isLeftAssociative = true;
        if (token.matches("intersection")) isLeftAssociative = true;
        if (token.matches("difference")) isLeftAssociative = true;
        if (token.matches("rename")) isLeftAssociative = true;
        if (token.matches(":=")) isLeftAssociative = true;
        if (token.matches("alias")) isLeftAssociative = true;
        if (token.matches("\\+")) isLeftAssociative = true;
        if (token.matches("-")) isLeftAssociative = true;
        if (token.matches("\\*")) isLeftAssociative = true;
        if (token.matches("/")) isLeftAssociative = true;
        if (token.matches("and")) isLeftAssociative = true;
        if (token.matches("or")) isLeftAssociative = true;
        if (token.matches("not")) isLeftAssociative = true;
        if (token.matches("<")) isLeftAssociative = true;
        if (token.matches("<=")) isLeftAssociative = true;
        if (token.matches(">")) isLeftAssociative = true;
        if (token.matches(">=")) isLeftAssociative = true;
        if (token.matches("=")) isLeftAssociative = true;
        if (token.matches("<>")) isLeftAssociative = true;
        return isLeftAssociative;
    }

    // given that a token is an operator, determine if right associative
    boolean tokenOperatorIsRightAssociative(String token) {
        return !tokenOperatorIsLeftAssociative(token);
    }

    // given operator, return operator precedence
    int tokenOperatorPrecedence(String token) {
        int precedence = 0;
        //if (token.matches("select")) precedence = 1;
        if (token.matches("load")) precedence = 2;
        if (token.matches("show")) precedence = 2;
        if (token.matches("where")) precedence = 1;
        if (token.matches("over")) precedence = 1;
        if (token.matches("join")) precedence = 1;
        if (token.matches("by")) precedence = 1;
        if (token.matches("times")) precedence = 1;
        if (token.matches("union")) precedence = 1;
        if (token.matches("intersection")) precedence = 1;
        if (token.matches("difference")) precedence = 1;
        if (token.matches("rename")) precedence = 1;
        if (token.matches(":=")) precedence = 0;
        if (token.matches("alias")) precedence = 0;
        if (token.matches("_")) precedence = 13;
        if (token.matches("\\+")) precedence = 11;
        if (token.matches("-")) precedence = 11;
        if (token.matches("\\*")) precedence = 12;
        if (token.matches("/")) precedence = 12;
        if (token.matches("and")) precedence = 4;
        if (token.matches("or")) precedence = 3;
        if (token.matches("not")) precedence = 13;
        if (token.matches("<")) precedence = 9;
        if (token.matches("<=")) precedence = 9;
        if (token.matches(">")) precedence = 9;
        if (token.matches(">=")) precedence = 9;
        if (token.matches("=")) precedence = 8;
        if (token.matches("<>")) precedence = 8;
        return precedence;
    }
    
    // given that a token is an operator, determine if it is unary
    boolean tokenOperatorIsUnary(String token) {
        boolean isUnary = false;
        //if (token.matches("select")) isUnary = true;
        //if (token.matches("where")) isUnary = true;
        if (token.matches("load")) isUnary = true;
        if (token.matches("show")) isUnary = true;
        if (token.matches("not")) isUnary = true;
        if (token.matches("_")) isUnary = true;
        return isUnary;
    }
    
    // given that a token is an operator, determine if it is binary
    boolean tokenOperatorIsBinary(String token) {
        return !tokenOperatorIsUnary(token);
    }

    // given an operator, determine if it is a unary prefix operator
    boolean tokenOperatorIsUnaryPrefix(String token) {
        if (!tokenOperatorIsUnary(token)) return false;
        boolean isUnaryPrefix = false;
        if (token.matches("select")) isUnaryPrefix = true;
        //if (token.matches("where")) isUnaryPrefix = true;
        if (token.matches("load")) isUnaryPrefix = true;
        if (token.matches("show")) isUnaryPrefix = true;
        if (token.matches("not")) isUnaryPrefix = true;
        if (token.matches("_")) isUnaryPrefix = true;
        return isUnaryPrefix;
    }

    // given an operator, determine if it is a unary postfix operator
    boolean tokenOperatorIsUnaryPostfix(String token) {
        if (!tokenOperatorIsUnary(token)) return false;
        return !tokenOperatorIsUnaryPrefix(token);
    }

    // determine if token is a pure operand
    boolean tokenIsOperand(String token) {
        boolean isNumber = false;
        if (!tokenIsOperator(token) && !tokenIsParenthesis(token)) isNumber = true;
        return isNumber;
    }

    // determine if token is left parenthesis
    boolean tokenIsLeftParenthesis(String token) {
        boolean isLeftParenthesis = false;
        if (token.matches("\\(")) isLeftParenthesis = true;
        return isLeftParenthesis;
    }

    // determine if token is right parenthesis
    boolean tokenIsRightParenthesis(String token) {
        boolean isRightParenthesis = false;
        if (token.matches("\\)")) isRightParenthesis = true;
        return isRightParenthesis;
    }

    // determine if token is (left or right) parenthesis
    boolean tokenIsParenthesis(String token) {
        return tokenIsLeftParenthesis(token) || tokenIsRightParenthesis(token);
    }

    // convert infix string to postfix stack of strings 
    // using the shunting yard algorithm
    Stack<String> shuntingYardAlgorithm(String expression) {
        // tokenise the expression
        ArrayList<String> tokens = tokenise(expression);
        // instantiate new stacks
        Stack<String> operatorStack = new Stack<String>();
        Stack<String> outputQueue = new Stack<String>();
        // iterate over the tokens
        Iterator iter = tokens.iterator();
        while (iter.hasNext()) {
            String token = (String)iter.next();
            // check to see if this token is an operand
            if (tokenIsOperand(token)) {
                // if so, add to the output queue
                outputQueue.push(token);
            }
            // check to see if this token is unary postfix
            if (tokenIsOperator(token) && tokenOperatorIsUnaryPostfix(token)) {
                // it is, so push it to the output queue
                outputQueue.push(token);
            }
            // check to see if this token is unary prefix
            if (tokenIsOperator(token) && tokenOperatorIsUnaryPrefix(token)) {
                // it is, so it gets pushed to the operator stack
                operatorStack.push(token);
            }
            // check to see if this token is a binary operator
            if (tokenIsOperator(token) && tokenOperatorIsBinary(token)) {
                // ok, it is an operator - so call it operator1
                String operator1 = token;
                // check the stack
                if (!operatorStack.empty()) {
                    // but is it an operator? could be parenthesis
                    if (tokenIsOperator(operatorStack.peek())) {
                    // ok, so there's an operator on top of the stack, call it operator2
                    String operator2 = operatorStack.peek();
                    // decide whether to pop operator2
                    boolean popOperator2 = false;
                    if (tokenOperatorIsLeftAssociative(operator1) && tokenOperatorPrecedence(operator1) <= tokenOperatorPrecedence(operator2)) popOperator2 = true;
                    if (tokenOperatorIsRightAssociative(operator1) && tokenOperatorPrecedence(operator1) < tokenOperatorPrecedence(operator2)) popOperator2 = true;
                    // pop if required
                    if (popOperator2) outputQueue.push(operatorStack.pop());
                    }
                }
                // in any case, now push operator1 onto the operator stack
                operatorStack.push(operator1);
            }
            // check to see if token is left parenthesis
            if (tokenIsLeftParenthesis(token)) {
                // if so, push to operator stack
                operatorStack.push(token);
            }
            // check to see if token is right parenthesis
            if (tokenIsRightParenthesis(token)) {
                // pop operators until reach left parenthesis
                boolean leftFound = false;
                while (!operatorStack.empty() && !leftFound) {
                    String topOfStack = operatorStack.pop();
                    if (tokenIsOperator(topOfStack)) outputQueue.push(topOfStack);
                    if (tokenIsLeftParenthesis(topOfStack)) leftFound = true;
                }
                if (!leftFound) setError(1,"mismatched parenthesis");
            }
        }
        // no more tokens to read so clear up operators on the operator stack
        while (!operatorStack.empty()) {
            // pop the operator onto the output queue
            String topOfStack = operatorStack.pop();
            if (tokenIsParenthesis(topOfStack)) setError(1,"mismatched parenthesis");
            outputQueue.push(topOfStack);
        }
        // return the output queue
        return outputQueue;
    }

    // given a stack, reverse it and return the result
    Stack<String> stackReverse(Stack<String> forwardStack) {
        Stack<String> reversed = new Stack<String>();
        while (!forwardStack.empty()) reversed.push(forwardStack.pop());
        return reversed;
    }

    // determine whether a token is a string literal
    // i.e. is it enclosed by quote marks
    boolean tokenIsQuoteEnclosed(String token) {
        char chStart = token.charAt(0);
        char chEnd = token.charAt(token.length()-1);
        if (chStart == 34 && chEnd == 34) return true;
        return false;
    }

    // remove all quote marks from a string
    String tokenRemoveQuotes(String token) {
        token = token.replaceAll("\"", "");
        return token;
    }

    // determine whether an operand is a number by trying a conversion
    boolean evaluateOperandIsNumber(String operand) {
        if (operand.startsWith("_") && operand.length() > 1) {
            operand = operand.substring(1);
        }
        boolean isNumber = true;
        try {
            double opNumber = (Double.parseDouble(operand));
        }
        catch (Exception e) {
            isNumber = false;
        }
        return isNumber;
    }

    // determine whether an operand is a boolean truth value
    boolean evaluateOperandIsBoolean(String operand) {
        boolean isBoolean = false;
        if (operand.matches("true")) isBoolean = true;
        if (operand.matches("false")) isBoolean = true;
        return isBoolean;
    }

    // convert an operand string into a number
    double evaluateOperandNumber(String operand) {
        double opNumber = 0.0;
        try {
            opNumber = (Double.parseDouble(operand));
        }
        catch (Exception e) {
            setError(3,"value '" + operand + "' expected to be number");
        }
        return opNumber;
    }

    // convert a string representing a boolean value into a boolean
    boolean evaluateOperandAsBoolean(String operand) {
        boolean result = false;
        if (operand.matches("true")) result = true;
        return result;
    }

    // evaluate the equality operator for a number or a string
    String evaluateEquality(Stack<String>operandQueue) {
        // initialise a result
        String result = "";
        // fetch binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate equality");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate equality");
        // convert to numbers - if possible
        boolean bothNumbers = false;
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // both are numbers - so compare as such
            bothNumbers = true;
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number1 == number2) result = "true"; else result = "false";
        } else {
            // otherwise compare as strings
            if (tokenIsQuoteEnclosed(operand1)) operand1 = tokenRemoveQuotes(operand1);
            if (tokenIsQuoteEnclosed(operand2)) operand2 = tokenRemoveQuotes(operand2);
            if (operand1.matches(operand2)) result = "true"; else result = "false";
        }
        // return result
        return result;
    }

    // evaluate the inequality operator for a number or a string
    String evaluateInequality(Stack<String>operandQueue) {
        // initialise a result
        String result = "";
        // fetch binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate inequality");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate inequality");
        // convert to numbers - if possible
        boolean bothNumbers = false;
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // both are numbers - so compare as such
            bothNumbers = true;
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number1 != number2) result = "true"; else result = "false";
        } else {
            // otherwise compare as strings
            if (tokenIsQuoteEnclosed(operand1)) operand1 = tokenRemoveQuotes(operand1);
            if (tokenIsQuoteEnclosed(operand2)) operand2 = tokenRemoveQuotes(operand2);
            if (!operand1.matches(operand2)) result = "true"; else result = "false";
        }
        // return result
        return result;
    }

    // evaluate the less than operator for a number or a string
    String evaluateLessThan(Stack<String>operandQueue) {
        // initialise a result
        String result = "";
        // fetch binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate less than");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate less than");
        // convert to numbers - if possible
        boolean bothNumbers = false;
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // both are numbers - so compare as such
            bothNumbers = true;
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number1 < number2) result = "true"; else result = "false";
        } else {
            // otherwise compare as strings
            if (tokenIsQuoteEnclosed(operand1)) operand1 = tokenRemoveQuotes(operand1);
            if (tokenIsQuoteEnclosed(operand2)) operand2 = tokenRemoveQuotes(operand2);
            if (operand1.compareTo(operand2) < 0) result = "true"; else result = "false";
        }
        // return result
        return result;
    }

    // evaluate the greater than operator for a number or a string
    String evaluateGreaterThan(Stack<String>operandQueue) {
        // initialise a result
        String result = "";
        // fetch binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate greater than");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate greater than");
        // convert to numbers - if possible
        boolean bothNumbers = false;
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // both are numbers - so compare as such
            bothNumbers = true;
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number1 > number2) result = "true"; else result = "false";
        } else {
            // otherwise compare as strings
            if (tokenIsQuoteEnclosed(operand1)) operand1 = tokenRemoveQuotes(operand1);
            if (tokenIsQuoteEnclosed(operand2)) operand2 = tokenRemoveQuotes(operand2);
            if (operand1.compareTo(operand2) > 0) result = "true"; else result = "false";
        }
        // return result
        return result;
    }

    // evaluate the less than or equals operator for a number or a string
    String evaluateLessThanOrEquals(Stack<String>operandQueue) {
        // initialise a result
        String result = "";
        // fetch binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate less than or equals");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate less than or equals");
        // convert to numbers - if possible
        boolean bothNumbers = false;
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // both are numbers - so compare as such
            bothNumbers = true;
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number1 <= number2) result = "true"; else result = "false";
        } else {
            // otherwise compare as strings
            if (tokenIsQuoteEnclosed(operand1)) operand1 = tokenRemoveQuotes(operand1);
            if (tokenIsQuoteEnclosed(operand2)) operand2 = tokenRemoveQuotes(operand2);
            if (operand1.compareTo(operand2) <= 0) result = "true"; else result = "false";
        }
        // return result
        return result;
    }

    // evaluate the greater than or equals operator for a number or a string
    String evaluateGreaterThanOrEquals(Stack<String>operandQueue) {
        // initialise a result
        String result = "";
        // fetch binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate greater than or equals");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands to evaluate greater than or equals");
        // convert to numbers - if possible
        boolean bothNumbers = false;
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // both are numbers - so compare as such
            bothNumbers = true;
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number1 >= number2) result = "true"; else result = "false";
        } else {
            // otherwise compare as strings
            if (tokenIsQuoteEnclosed(operand1)) operand1 = tokenRemoveQuotes(operand1);
            if (tokenIsQuoteEnclosed(operand2)) operand2 = tokenRemoveQuotes(operand2);
            if (operand1.compareTo(operand2) >= 0) result = "true"; else result = "false";
        }
        // return result
        return result;
    }

    // evaluate logical not
    String evaluateLogicalNot(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the unary operand
        String operand = "";
        if (!operandQueue.empty()) operand = operandQueue.pop(); else setError(1,"no operand found for logical not");
        // check that the operand is a boolean value
        if (evaluateOperandIsBoolean(operand)) {
            // if true, then result is false and vice versa
            if (evaluateOperandAsBoolean(operand)) result = "false"; else result = "true";
        } else {
            // operand was not a boolean
            setError(2,"operand '" + operand + "' found but boolean expected");
        }
        // return result
        return result;
    }

    // evaluate logical and
    String evaluateLogicalAnd(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands for logical and");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands for logical and");
        // check that both operands are boolean values
        if (evaluateOperandIsBoolean(operand1) && evaluateOperandIsBoolean(operand2)) {
            // evaluate logical operator
            boolean bool1 = evaluateOperandAsBoolean(operand1);
            boolean bool2 = evaluateOperandAsBoolean(operand2);
            if (bool1 && bool2) result = "true"; else result = "false";
        } else {
            // error state
            if (!evaluateOperandIsBoolean(operand1)) setError(2,"operand '" + operand1 + "' found but boolean expected by logical and");
            if (!evaluateOperandIsBoolean(operand2)) setError(2,"operand '" + operand2 + "' found but boolean expected by logical and");
        }
        // return the result
        return result;
    }

    // evaluate logical or
    String evaluateLogicalOr(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands for logical or");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands for logical or");
        // check that both operands are boolean values
        if (evaluateOperandIsBoolean(operand1) && evaluateOperandIsBoolean(operand2)) {
            // evaluate logical operator
            boolean bool1 = evaluateOperandAsBoolean(operand1);
            boolean bool2 = evaluateOperandAsBoolean(operand2);
            if (bool1 || bool2) result = "true"; else result = "false";
        } else {
            // error state
            if (!evaluateOperandIsBoolean(operand1)) setError(2,"operand '" + operand1 + "' found but boolean expected by logical or");
            if (!evaluateOperandIsBoolean(operand2)) setError(2,"operand '" + operand2 + "' found but boolean expected by logical or");
        }
        // return the result
        return result;
    }

    // evaluate unary negation
    String evaluateUnaryNegation(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the unary operand
        String operand = "";
        if (!operandQueue.empty()) operand = operandQueue.pop(); else setError(1,"no operand found for numerical negation");
        // check the operand is a numbers
        if (evaluateOperandIsNumber(operand)) {
            // evaluate unary negation operator
            double number = -1.0 * evaluateOperandNumber(operand);
            result = String.valueOf(number);
        } else {
            // error state
            setError(2,"operand '" + operand + "' found but number expected for numerical negation");
        }
        // return the result
        return result;
    }

    // evaluate addition
    String evaluateAddition(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands for numerical addition");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands for numerical addition");
        // check that both operands are numbers
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // evaluate logical operator
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            result = String.valueOf(number1 + number2);
        } else {
            // error state
            if (!evaluateOperandIsNumber(operand1)) setError(2,"operand '" + operand1 + "' found but number expected for numerical addition");
            if (!evaluateOperandIsNumber(operand2)) setError(2,"operand '" + operand2 + "' found but number expected for numerical addition");            
        }
        // return the result
        return result;
    }

    // evaluate subtraction
    String evaluateSubtraction(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands for numerical subtraction");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands for numerical subtraction");
        // check that both operands are numbers
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // evaluate logical operator
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            result = String.valueOf(number1 - number2);
        } else {
            // error state
            if (!evaluateOperandIsNumber(operand1)) setError(2,"operand '" + operand1 + "' found but number expected for numerical subtraction");
            if (!evaluateOperandIsNumber(operand2)) setError(2,"operand '" + operand2 + "' found but number expected for numerical subtraction");
        }
        // return the result
        return result;
    }

    // evaluate multiplication
    String evaluateMultiplication(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands for numerical multiplication");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands for numerical multiplication");
        // check that both operands are numbers
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // evaluate logical operator
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            result = String.valueOf(number1 * number2);
        } else {
            // error state
            if (!evaluateOperandIsNumber(operand1)) setError(2,"operand '" + operand1 + "' found but number expected for numerical multiplication");
            if (!evaluateOperandIsNumber(operand2)) setError(2,"operand '" + operand2 + "' found but number expected for numerical multiplication");            
        }
        // return the result
        return result;
    }

    // evaluate division (mathematical not relational)
    String evaluateMathDivision(Stack<String> operandQueue) {
        // initialise a result
        String result = "";
        // fetch the binary operands
        String operand1 = "";
        String operand2 = "";
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else setError(1,"insufficient operands for numerical division");
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else setError(1,"insufficient operands for numerical division");
        // check that both operands are numbers
        if (evaluateOperandIsNumber(operand1) && evaluateOperandIsNumber(operand2)) {
            // evaluate logical operator
            double number1 = evaluateOperandNumber(operand1);
            double number2 = evaluateOperandNumber(operand2);
            if (number2 != 0) {
                // calculate
                result = String.valueOf(number1 / number2);
            } else {
                // was attempt to divide by zero
                setError(2,"cannot divide by zero");
            }
        } else {
            // error state
            if (!evaluateOperandIsNumber(operand1)) setError(2,"operand '" + operand1 + "' found but number expected for numerical divison");
            if (!evaluateOperandIsNumber(operand2)) setError(2,"operand '" + operand2 + "' found but number expected for numerical divison");            
        }
        // return the result
        return result;
    }

    // evaluate relational projection
    String evaluateProject(Stack<String> operandQueue) {
        // fetch the operands
        ArrayList<String> projAttrs = new ArrayList<String>();
        // first operand is always a projected attribute
        projAttrs.add(operandQueue.pop());
        // now keep getting them until run out of commas
        while (operandQueue.peek().matches("comma")) {
            operandQueue.pop();
            projAttrs.add(operandQueue.pop());
        }
        // final operand is the relation to project from
        String result = "";
        String projName = operandQueue.pop();
        // check that relation exists
        if (definedNameExists(projName)) {
            Relation subjectRelation = definedRelation(projName);
            // build a projection string
            String attributes = "";
            boolean allExist = true;
            Iterator iter = projAttrs.iterator();
            while (iter.hasNext()) {
                String attr = (String)iter.next();
                if (!subjectRelation.attributeNameExists(attr)) {
                    allExist = false;
                    setError (2, "attribute '" + attr + "' not found in relation '" + projName + "'");
                }
                attributes = attributes.concat(attr);
                if (iter.hasNext()) attributes = attributes.concat(", ");
            }
            // carry out the projection into an intermediate result
            if (allExist) {
                // all attributes exist - this is a valid projection
                result = definedNextName();
                Relation resultRelation = subjectRelation.project(attributes);
                definedAssign(result,resultRelation);
            }

        } else {
            // attempt to project on non existent relation
            setError(2,"relation '" + projName + "' not found");
        }
        // return the result
        return result;
    }

    // evaluate relational select
    String evaluateSelect(Stack<String> operandQueue) {
        // fetch the operands
        Stack<String> selectQueue = new Stack<String>();
        // keep getting operands until run out
        boolean keepGoing = !operandQueue.empty();
        while (keepGoing) {
            selectQueue.push(operandQueue.pop());
            // stop if the operand queue is empty
            keepGoing = !operandQueue.empty();
            // also stop if the last operand was a relation name
            if (definedNameExists(selectQueue.peek())) keepGoing = false;
        }
        // but the last operand was the relation name for the select
        String selectName = selectQueue.pop();
        String result = "";
        if (definedNameExists(selectName) && !selectQueue.empty()) {
            // carry out the selection into an intermediate result
            result = definedNextName();
            Relation subjectRelation = definedRelation(selectName);
            Relation resultRelation = subjectRelation.selectPostfixStack(selectQueue);
            definedAssign(result,resultRelation);  
        } else {
            // error state
            setError(2,"relation '" + selectName + "' not found");
            if (selectQueue.empty()) setError(2,"insufficient operands for select clause in where operator");
        }
        return result;
    }

    // evaluation relational join
    String evaluateJoin(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        if (definedNameExists(operand1) && definedNameExists(operand2) && !tokenError) {
            Relation opRelation1 = definedRelation(operand1);
            Relation opRelation2 = definedRelation(operand2);
            result = definedNextName();
            Relation resultRelation = opRelation1.join(opRelation2);
            definedAssign(result,resultRelation);
        } else {
            // error state
            if (!definedNameExists(operand1)) setError(2,"relation '" + operand1 + "' not found");
            if (!definedNameExists(operand2)) setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for join operator");
        }

        return result;
    }

    // evaluate relational divide
    String evaluateDivide(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        if (definedNameExists(operand1) && definedNameExists(operand2) && !tokenError) {
            Relation opRelation1 = definedRelation(operand1);
            Relation opRelation2 = definedRelation(operand2);
            result = definedNextName();
            Relation resultRelation = opRelation1.divide(opRelation2);
            definedAssign(result,resultRelation);
        } else {
            // error state
            if (!definedNameExists(operand1)) setError(2,"relation '" + operand1 + "' not found");
            if (!definedNameExists(operand2)) setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for divide operator");
        }
        return result;
    }

    // evaluate relational times
    String evaluateTimes(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        if (definedNameExists(operand1) && definedNameExists(operand2) && !tokenError) {
            Relation opRelation1 = definedRelation(operand1);
            Relation opRelation2 = definedRelation(operand2);
            result = definedNextName();
            Relation resultRelation = opRelation1.times(opRelation2);
            definedAssign(result,resultRelation);
        } else {
            // error state
            if (!definedNameExists(operand1)) setError(2,"relation '" + operand1 + "' not found");
            if (!definedNameExists(operand2)) setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for times operator");
        }      
        return result;
    }

    // evaluate relational union
    String evaluateUnion(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        if (definedNameExists(operand1) && definedNameExists(operand2) && !tokenError) {
            Relation opRelation1 = definedRelation(operand1);
            Relation opRelation2 = definedRelation(operand2);
            result = definedNextName();
            Relation resultRelation = opRelation1.union(opRelation2);
            definedAssign(result,resultRelation);
        } else {
            // error state
            if (!definedNameExists(operand1)) setError(2,"relation '" + operand1 + "' not found");
            if (!definedNameExists(operand2)) setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for union operator");
        }
        return result;
    }

    // evaluate relational intersection
    String evaluateIntersection(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        if (definedNameExists(operand1) && definedNameExists(operand2) && !tokenError) {
            Relation opRelation1 = definedRelation(operand1);
            Relation opRelation2 = definedRelation(operand2);
            result = definedNextName();
            Relation resultRelation = opRelation1.intersection(opRelation2);
            definedAssign(result,resultRelation);
        } else {
            // error state
            if (!definedNameExists(operand1)) setError(2,"relation '" + operand1 + "' not found");
            if (!definedNameExists(operand2)) setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for intersection operator");
        }
        return result;
    }

    // evaluate relational difference
    String evaluateDifference(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        if (definedNameExists(operand1) && definedNameExists(operand2) && !tokenError) {
            Relation opRelation1 = definedRelation(operand1);
            Relation opRelation2 = definedRelation(operand2);
            result = definedNextName();
            Relation resultRelation = opRelation1.difference(opRelation2);
            definedAssign(result,resultRelation);
        } else {
            // error state
            if (!definedNameExists(operand1)) setError(2,"relation '" + operand1 + "' not found");
            if (!definedNameExists(operand2)) setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for difference operator");
        }
        return result;
    }
    
    // evaluate assignment
    String evaluateAssignment(Stack<String> operandQueue) {
        String operand1 = "";
        String operand2 = "";
        boolean tokenError = false;
        if (!operandQueue.empty()) operand2 = operandQueue.pop(); else tokenError = true;
        if (!operandQueue.empty()) operand1 = operandQueue.pop(); else tokenError = true;
        String result = "";
        // if operand2 exists as a relation then make a copy
        if (definedNameExists(operand2) && !tokenError) {
            Relation opRelation2 = definedRelation(operand2);
            Relation resultRelation = opRelation2.clone();
            // if operand1 already exists as a relation then get rid of it
            if (definedNameExists(operand1)) definedRemove(operand1);
            // now assign the result relation to operand1
            definedAssign(operand1,resultRelation);
            // result is the name of the new relation
            result = operand1;
        } else {
            // operand2 does not exist so cannot copy
            setError(2,"relation '" + operand2 + "' not found");
            if (tokenError) setError(1,"insufficient operands for assignment or alias");
        }
        return result;
    }
    
    // save a relation to a CSV file
    public void saveCSV(String filename) {
        File file = new File(filename);
        FileWriter fileWriter = null;
        BufferedWriter buffWriter = null;
        try {
            fileWriter = new FileWriter(file,false);
            buffWriter = new BufferedWriter(fileWriter);
            Iterator iter = this.attributes.iterator();
            String attrStr = "";
            String domainStr = "";
            String lineSep = System.getProperty("line.separator");
            ArrayList<String> attrs = new ArrayList<String>();
            while (iter.hasNext()) {
                RelationAttribute attr = (RelationAttribute)iter.next();
                attrStr = attrStr + "\"" + attr.getName() + "\"";
                attrs.add(attr.getName());
                domainStr = domainStr + "\"" + attr.getDomain() + "\"";
                if (iter.hasNext()) {
                    attrStr = attrStr + ",";
                    domainStr = domainStr + ",";
                } else {
                    attrStr = attrStr + lineSep;
                    domainStr = domainStr + lineSep;
                }

            }
            buffWriter.write(attrStr);
            buffWriter.write(domainStr);
            buffWriter.flush();
            iter = this.tuples.iterator();
            while (iter.hasNext()) {
                RelationTuple tuple = (RelationTuple)iter.next();
                String tupStr = "";
                Iterator attrIter = attrs.iterator();
                while (attrIter.hasNext()) {
                    String value = tuple.getValue((String)attrIter.next());
                    tupStr = tupStr + "\"" + value + "\"";
                    if (attrIter.hasNext()) {
                        tupStr = tupStr + ",";
                    } else {
                        tupStr = tupStr + lineSep;
                    }
                }
                buffWriter.write(tupStr);
                buffWriter.flush();
            }        

            
        }        
        catch (IOException e) {
            setError(3,"file operation failed: " + e.getMessage());
        }            
        finally {
            try {
                if (fileWriter != null) fileWriter.close();
            } catch (IOException e) { setError(3,"file operation failed: " + e.getMessage()); }
        } 

    }

    // load a relation from a CSV file
    public Relation loadCSV(String filename) {
        Relation result = new Relation();
        File file = new File(filename);
        FileReader fileReader = null;
        BufferedReader buffReader = null;
        String line = null;
        int row = 0;
        ArrayList<String> attrNames = new ArrayList<String>();
        ArrayList<String> domnNames = new ArrayList<String>();
        try {
            fileReader = new FileReader(file);
            buffReader = new BufferedReader(fileReader);    
            while ((line = buffReader.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(line,",");
                int attrNum = 0;
                RelationTuple newTuple = null;
                if (row > 1) newTuple = new RelationTuple(result);
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    token = token.replaceAll("\"", "");
                    if (row == 0) attrNames.add(attrNum,token);
                    if (row == 1) domnNames.add(attrNum,token);
                    if (row > 1) {
                        // load value into attribute
                        String attrName = attrNames.get(attrNum);
                        newTuple.setValue(attrName, token);
                    }
                    attrNum++;
                }
                if (row == 1) {
                    // header now read so create the attrs and domains
                    Iterator attrIter = attrNames.iterator();
                    Iterator domnIter = domnNames.iterator();
                    while (attrIter.hasNext()) {
                        String attrName = (String)attrIter.next();
                        String domnName = "";
                        if (domnIter.hasNext()) domnName = (String)domnIter.next();
                        result.addAttribute(attrName, domnName);
                    }
                }
                if (row > 1) {
                    // load tuple into relation
                    result.addTuple(newTuple);
                }
                row++;
            }
        } catch (Exception e) {
            setError(3,"file operation failed: " + e.getMessage());
        }
        finally {
            try {
                if (fileReader != null) fileReader.close();
            } catch (IOException e) { setError(3,"file operation failed: " + e.getMessage()); }
        }
        result.removeDuplicateTuples();
        return result;
    }

    // evaluate load operand
    String evaluateLoad(Stack<String> operandQueue) {
        String operand = "";
        String result = "";
        if (!operandQueue.empty()) {
            operand = operandQueue.pop();
            if (tokenIsQuoteEnclosed(operand)) operand = tokenRemoveQuotes(operand);
            result = definedNextName();
            Relation resRelation = loadCSV(operand);
            definedAssign(result,resRelation);
        } else {
           setError(1,"insufficient operands for load command");
        }
        return result;
    }

    // returns a string padded with spaces to the specified number of characters
    String spacePad(String str, int toLength) {
        //if (str.length() >= toLength) return str;
        while (str.length() < toLength) str = str.concat(" ");
        return str;
    }

    // evaluate the show operand
    String evaluateShow(Stack<String> operandQueue) {
        String operand = "";
        String result = "";
        if (operandQueue.empty()) {
            setError(1,"insufficient operands for show command");
        } else {
            operand = operandQueue.pop();
            if (!definedNameExists(operand)) {
                // error state
                setError(2,"relation '" + operand + "' not found");
            } else {
                // build the table
                Relation opRelation = definedRelation(operand);
                int maxWidth[] = new int[opRelation.degree()];
                for (int s=0; s<opRelation.degree(); s++) maxWidth[s] = 5;
                String grid[][] = new String[opRelation.degree()][opRelation.cardinality()+2];
                // add attributes and domains into the grid
                Iterator iter = opRelation.attributes.iterator();
                int col = 0;
                while (iter.hasNext()) {
                    RelationAttribute attr = (RelationAttribute)iter.next();
                    grid[col][0] = attr.getName();
                    if (attr.getName().length() > maxWidth[col]) maxWidth[col] = attr.getName().length();
                    grid[col][1] = attr.getDomain();
                    if (attr.getDomain().length() > maxWidth[col]) maxWidth[col] = attr.getDomain().length();
                    col++;
                }
                // add tuples into the grid
                iter = opRelation.tuples.iterator();
                int row = 2;
                while (iter.hasNext()) {
                    RelationTuple tuple = (RelationTuple)iter.next();
                    col = 0;
                    Iterator attrIter = opRelation.attributes.iterator();
                    while (attrIter.hasNext()) {
                        RelationAttribute attr = (RelationAttribute)attrIter.next();
                        String value = tuple.getValue(attr.getName());
                        if (value.length() > maxWidth[col]) maxWidth[col] = value.length();
                        grid[col][row] = value;
                        col++;
                    }
                    row++;
                }
                // format the grid as a string for display
                for (int j=0; j<opRelation.cardinality()+2; j++) {
                    for (int i=0; i<opRelation.degree(); i++) {
                        String field = grid[i][j];
                        field = spacePad(field,maxWidth[i]+1);
                        result = result + field;
                    }
                    result = result + "\n";
                }
                // tag for empty relations
                if (opRelation.cardinality() == 0) result = result + "<<<is empty>>>\n";
            }
        }
        // return result
        return result;
    }

    // evaluate relational rename
    String evaluateRename(Stack<String> operandQueue) {
        ArrayList<String> attrNames = new ArrayList<String>();
        ArrayList<String> toNames = new ArrayList<String>();
        boolean listEnded = false;
        boolean tokenError = false;
        while (!operandQueue.empty() && !listEnded) {
            String attrName = "";
            String toName = "";
            String asToken = "";
            if (!operandQueue.empty()) {
                toName = operandQueue.pop();
            } else {
                tokenError = true;
                setError(1,"insufficient operands for rename operator");
            }
            if (!operandQueue.empty()) {
                asToken = operandQueue.pop();
            }
            if (!operandQueue.empty()) {
                attrName = operandQueue.pop();
            } else {
                tokenError = true;
                setError(1,"insufficient operands for rename operator");
            }
            if (!asToken.matches("as")) {
                tokenError = true;
                setError(2,"rename expected 'as' but met '" + asToken + "'");
            }
            if (attrName.length()>0 && toName.length()>0) {
                attrNames.add(attrName);
                toNames.add(toName);
            }
            if (!operandQueue.peek().matches("comma")) listEnded = true; else operandQueue.pop();
        }
        String result = "";
        if (!tokenError) {
            String relName = "";
            if (!operandQueue.empty()) relName = operandQueue.pop();
            Relation renRelation = null;
            result = "";
            if (definedNameExists(relName)) {
                renRelation = definedRelation(relName).clone();
                result = definedNextName();
                definedAssign(result,renRelation);        
                Iterator iter = attrNames.iterator();
                Iterator toIter = toNames.iterator();
                while (iter.hasNext()) {
                    String attrName = (String)iter.next();
                    if (!renRelation.attributeNameExists(attrName)) {
                        setError(2,"cannot rename attribute from '" + attrName + "' - no such attribute in relation '" + relName + "'");
                    } else {
                        String toName = "ERR";
                        if (toIter.hasNext()) toName = (String)toIter.next();
                        if (renRelation.attributeNameExists(toName)) {
                            setError(2,"cannot rename attribute to '" + toName + "' because this attribute already exists in relation '" + relName + "'");
                        } else {
                            renRelation.renameAttribute(attrName, toName);
                        } 
                    }
                }
            } else {
                // error state
                setError(2,"relation '" + relName + "' not found in rename");
            }
        }
        return result;
    }

    // remove tokens that do not trigger evaluations
    Stack<String> removeSurplusTokens(Stack<String> postfixStack) {
        Stack<String> newStack = new Stack<String>();
        Iterator iter = postfixStack.iterator();
        while (iter.hasNext()) {
            String token = (String)iter.next();
            boolean surplus = false;
            if (token.matches("select")) surplus = true;
            if (token.matches("project")) surplus = true;
            if (token.matches("divide")) surplus = true;
            if (!surplus) newStack.push(token);
        }
        // return result
        newStack = stackReverse(newStack);
        return newStack;
    }

    // debugging function - non-destructive dump of a stack
    void printStack(String name, Stack<String> aStack) {
        System.out.println("*** Stack " + name + ": ");
        Stack<String> bStack = (Stack<String>)aStack.clone();
        while (!bStack.empty()) System.out.print(bStack.pop() + " ");
        System.out.println();
    }

    // evaluate an expression against a tuple
    String evaluateExpression(Stack<String> postfixStack, RelationTuple tuple) {
        // initialise result
        String result = "";
        Stack<String> operandQueue = new Stack<String>();
        // take each token in the postfix stack
        while (!postfixStack.empty() && !errorCondition) {
            String token = postfixStack.pop();
            // check to see if the token is an operand
            if (tokenIsOperand(token)) {
                // carry out substitution if required
                boolean substituted = false;
                if (attributeNameExists(token)) {
                    token = tuple.getValue(token);
                    substituted = true;
                }
                if (!substituted && !tokenIsQuoteEnclosed(token) && !evaluateOperandIsNumber(token) && !evaluateOperandIsBoolean(token)) {
                    // error state - it wasn't subst so not an attribute, also not a string, nor a number, nor a boolean
                    setError(2,"attribute '" + token + "' not found in relation");
                }
                // push to operand queue
                operandQueue.push(token);
            }
            // check to see if the token is an operator
            if (tokenIsOperator(token)) {
                if (token.matches("_")) postfixStack.push(evaluateUnaryNegation(operandQueue));
                if (token.matches("\\+")) postfixStack.push(evaluateAddition(operandQueue));
                if (token.matches("-")) postfixStack.push(evaluateSubtraction(operandQueue));
                if (token.matches("\\*")) postfixStack.push(evaluateMultiplication(operandQueue));
                if (token.matches("/")) postfixStack.push(evaluateMathDivision(operandQueue));
                if (token.matches("and")) postfixStack.push(evaluateLogicalAnd(operandQueue));
                if (token.matches("or")) postfixStack.push(evaluateLogicalOr(operandQueue));
                if (token.matches("not")) postfixStack.push(evaluateLogicalNot(operandQueue));
                if (token.matches("=")) postfixStack.push(evaluateEquality(operandQueue));
                if (token.matches("<>")) postfixStack.push(evaluateInequality(operandQueue));
                if (token.matches("<")) postfixStack.push(evaluateLessThan(operandQueue));
                if (token.matches(">")) postfixStack.push(evaluateGreaterThan(operandQueue));
                if (token.matches("<=")) postfixStack.push(evaluateLessThanOrEquals(operandQueue));
                if (token.matches(">=")) postfixStack.push(evaluateGreaterThanOrEquals(operandQueue));
            }
            // check to see if postfix stack is now empty
            if (postfixStack.empty()) {
                // if so, this is the result
                result = token;
            }
        }
        if (!evaluateOperandIsBoolean(result)) setError(2,"expression does not evaluate to a boolean value, '" + result + "' instead");
        // evaluation complete
        return result;
    }

    // determine whether a token is a relational algebra operator
    boolean tokenIsAlgebraOperator(String token) {
        boolean isAlgebra = false;
        if (token.matches("load")) isAlgebra = true;
        if (token.matches("show")) isAlgebra = true;
        if (token.matches(":=")) isAlgebra = true;
        if (token.matches("alias")) isAlgebra = true;
        if (token.matches("over")) isAlgebra = true;
        if (token.matches("where")) isAlgebra = true;
        if (token.matches("join")) isAlgebra = true;
        if (token.matches("by")) isAlgebra = true;
        if (token.matches("times")) isAlgebra = true;
        if (token.matches("union")) isAlgebra = true;
        if (token.matches("intersection")) isAlgebra = true;
        if (token.matches("difference")) isAlgebra = true;
        if (token.matches("rename")) isAlgebra = true;
        return isAlgebra;
    }

    /**
     * Evalutes the string expression as relational algebra
     * @param expression the expression to be evaluated
     * @return the result as a string
     */
    public static String evaluate(String expression) {
        // clear the error condition flags
        clearError();
        // check that the evaluation environment exists
        if (definedRels == null) initDefinedRels();
        Relation eval = new Relation();
        // parse the expression
        String result = "";
        Stack<String> postfixStack = eval.shuntingYardAlgorithm(expression);
        postfixStack = eval.removeSurplusTokens(postfixStack);
        // evaluate
        Stack<String> operandQueue = new Stack<String>();
        while (!postfixStack.empty() && !errorCondition) {
            eval.printStack("iter", postfixStack);
            String token = postfixStack.pop();;
            // check to see if the token is an operator
            if (eval.tokenIsAlgebraOperator(token)) {
                // if it is an operator then evaluate as such
                if (token.matches("load")) postfixStack.push(eval.evaluateLoad(operandQueue));
                if (token.matches("show")) token = eval.evaluateShow(operandQueue);
                if (token.matches(":=")) postfixStack.push(eval.evaluateAssignment(operandQueue));
                if (token.matches("alias")) postfixStack.push(eval.evaluateAssignment(operandQueue));
                if (token.matches("over")) postfixStack.push(eval.evaluateProject(operandQueue));
                if (token.matches("where")) postfixStack.push(eval.evaluateSelect(operandQueue));
                if (token.matches("join")) postfixStack.push(eval.evaluateJoin(operandQueue));
                if (token.matches("by")) postfixStack.push(eval.evaluateDivide(operandQueue));
                if (token.matches("times")) postfixStack.push(eval.evaluateTimes(operandQueue));
                if (token.matches("union")) postfixStack.push(eval.evaluateUnion(operandQueue));
                if (token.matches("intersection")) postfixStack.push(eval.evaluateIntersection(operandQueue));
                if (token.matches("difference")) postfixStack.push(eval.evaluateDifference(operandQueue));
                if (token.matches("rename")) postfixStack.push(eval.evaluateRename(operandQueue));
            } else {
                // otherwise chuck it on the operand queue
                operandQueue.push(token);
            }
            // check to see if postfix stack is now emptry
            if (postfixStack.empty()) {
                // if so, this is the result
                result = token;
            }
        }
        // if result is a relation then show it
        if (definedNameExists(result))  {
            Stack<String> temp = new Stack<String>();
            temp.add(result);
            result = eval.evaluateShow(temp);
        }
        // garbage collect
        definedGarbageCollect();
        // return result
        return result;
    }


    //========================================================================
    // Miscellaneous
    //========================================================================
    
    // internal function to set an error condition
    static void setError(int errType, String errMessage) {
        errorType = errType;
        errorMessage = errMessage;
        errorCondition = true;
    }

    /**
     * Clear any error condition
     */
    static public void clearError() {
        errorCondition = false;
        errorMessage = "";
        errorType = 0;
    }

    /**
     * Determine whether an error condition has occurred
     * @return true if an error has occurred
     */
    static public boolean error() {
        return errorCondition;
    }

    /**
     * Returns an error message
     * @return error message
     */
    static public String errorMessage() {
        String result = "";
        if (!errorCondition) return result;
        if (errorType == 1) result = "Syntax error: ".concat(errorMessage);
        if (errorType == 2) result = "Semantic error: ".concat(errorMessage);
        if (errorType == 3) result = "System error: ".concat(errorMessage);
        if (result.length() == 0) result = "Unknown error";
        return result;
    }
    
}

