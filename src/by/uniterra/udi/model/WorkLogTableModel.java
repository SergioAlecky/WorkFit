package by.uniterra.udi.model;

public class WorkLogTableModel extends AbstractFlexTableModel
{

    /** TODO document <code>serialVersionUID</code> */
    private static final long serialVersionUID = -8488583506352518426L;

    private final static int COL_NAME = 0;
    private final static int COL_CURENT = 1;
    private final static int COL_TO_PLANE = 2;
    private final static int COL_TO_BONUS = 3;
    private final static int COL_REST_HOLIDAY = 4;
    
    public WorkLogTableModel()
    {
        addColumn(COL_NAME, "Name", String.class);
        addColumn(COL_CURENT, "Curent", String.class);
        addColumn(COL_TO_PLANE, "To plane", String.class);
        addColumn(COL_TO_BONUS, "To bonus", String.class);
        addColumn(COL_REST_HOLIDAY, "To holiday", String.class);
        
        
    }
    
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
    
    @Override
    public Object getValueById(int rowIndex, int columnId)
    {
        // TODO Auto-generated method stub
        return null;
    }
    

}