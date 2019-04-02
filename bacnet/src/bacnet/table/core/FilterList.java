package bacnet.table.core;

import java.util.ArrayList;
import java.util.TreeMap;
import bacnet.datamodel.dataset.ExpressionMatrix;


public class FilterList implements Cloneable {

    private TreeMap<String, Filter> filters = new TreeMap<String, Filter>();
    private ExpressionMatrix matrix;
    private ArrayList<String> excludeRow = new ArrayList<String>();
    private ArrayList<String> excludeColumn = new ArrayList<String>();


    public FilterList() {}

    public FilterList(ExpressionMatrix matrix) {
        this.matrix = matrix;
    }

    public void updateExclude() {
        excludeRow.clear();
        excludeColumn.clear();
        for (String filterName : filters.keySet()) {
            for (String row : filters.get(filterName).getExcludeRow()) {
                excludeRow.add(row);
            }
            for (String col : filters.get(filterName).getExcludeColumn()) {
                excludeColumn.add(col);
            }
        }
    }

    //
    // public ColorMapper getCorrespondingMapper(String header){
    // for(Type type : Type.values()){
    // if(header.contains(type.toString()+"_")){
    // return filters.get(type);
    // }
    // }
    // return filters.get(Type.OTHER);
    //
    // }
    //
    // public ColorMapper getFirstMapper(){
    // for(Type type : filters.keySet()){
    // return filters.get(type);
    // }
    // System.out.println("No ColorMapper found");
    // return null;
    // }
    //
    //
    //
    // @Override
    // public FilterList clone() {
    // FilterList cloned = new FilterList();
    // for(Type type : filters.keySet()){
    // cloned.getColorMappers().put(type, filters.get(type).clone());
    // }
    // return cloned;
    // }


    /*
     * ******************************************************** GETTERs and SETTERs
     * ********************************************************
     */

    public TreeMap<String, Filter> getFilters() {
        return filters;
    }

    public void setFilters(TreeMap<String, Filter> filters) {
        this.filters = filters;
    }

    public ExpressionMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(ExpressionMatrix matrix) {
        this.matrix = matrix;
    }

    public ArrayList<String> getExcludeRow() {
        return excludeRow;
    }

    public void setExcludeRow(ArrayList<String> excludeRow) {
        this.excludeRow = excludeRow;
    }

    public ArrayList<String> getExcludeColumn() {
        return excludeColumn;
    }

    public void setExcludeColumn(ArrayList<String> excludeColumn) {
        this.excludeColumn = excludeColumn;
    }



}
