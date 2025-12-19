// src/components/Analysis.tsx
import React, { useState, useEffect } from 'react';
import './Analysis.css';

const API_BASE_URL = 'http://localhost:8080/api/analysis';

const STORAGE_KEY = 'analysisFilters';

interface AnalysisRow {
    date: string;
    dauDB: number | null;
    db: number | null;
    dauG1: number | null;
    g1: number | null;
    dauDBMatch: number;
    dbMatch: number;
    dauG1Match: number;
    g1Match: number;
}

interface AnalysisData {
    rows: AnalysisRow[];
}

interface FilterState {
    fromDate: string;
    dauDBStart: string;
    dauDBEnd: string;
    dbStart: string;
    dbEnd: string;
    dauG1Start: string;
    dauG1End: string;
    g1Start: string;
    g1End: string;
}

// Helper functions để lưu và load từ localStorage
const loadFiltersFromStorage = (): FilterState | null => {
    try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
            return JSON.parse(stored);
        }
    } catch (error) {
        console.error('Error loading filters from storage:', error);
    }
    return null;
};

const saveFiltersToStorage = (filters: FilterState) => {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(filters));
    } catch (error) {
        console.error('Error saving filters to storage:', error);
    }
};

const getDefaultFilters = (): FilterState => {
    const date = new Date();
    date.setDate(date.getDate() - 30);
    return {
        fromDate: date.toISOString().split('T')[0],
        dauDBStart: '',
        dauDBEnd: '',
        dbStart: '',
        dbEnd: '',
        dauG1Start: '',
        dauG1End: '',
        g1Start: '',
        g1End: ''
    };
};

const Analysis: React.FC = () => {
    const [data, setData] = useState<AnalysisData | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    
    // Load filters from localStorage hoặc dùng default
    const storedFilters = loadFiltersFromStorage();
    const defaultFilters = getDefaultFilters();
    
    // Filters - khôi phục từ localStorage nếu có
    const [fromDate, setFromDate] = useState<string>(
        storedFilters?.fromDate || defaultFilters.fromDate
    );
    const [dauDBStart, setDauDBStart] = useState<string>(
        storedFilters?.dauDBStart || defaultFilters.dauDBStart
    );
    const [dauDBEnd, setDauDBEnd] = useState<string>(
        storedFilters?.dauDBEnd || defaultFilters.dauDBEnd
    );
    const [dbStart, setDbStart] = useState<string>(
        storedFilters?.dbStart || defaultFilters.dbStart
    );
    const [dbEnd, setDbEnd] = useState<string>(
        storedFilters?.dbEnd || defaultFilters.dbEnd
    );
    const [dauG1Start, setDauG1Start] = useState<string>(
        storedFilters?.dauG1Start || defaultFilters.dauG1Start
    );
    const [dauG1End, setDauG1End] = useState<string>(
        storedFilters?.dauG1End || defaultFilters.dauG1End
    );
    const [g1Start, setG1Start] = useState<string>(
        storedFilters?.g1Start || defaultFilters.g1Start
    );
    const [g1End, setG1End] = useState<string>(
        storedFilters?.g1End || defaultFilters.g1End
    );

    const fetchAnalysisData = async () => {
        setLoading(true);
        setError(null);
        
        try {
            const params = new URLSearchParams();
            if (fromDate) params.append('fromDate', fromDate);
            if (dauDBStart) params.append('dauDBStart', dauDBStart);
            if (dauDBEnd) params.append('dauDBEnd', dauDBEnd);
            if (dbStart) params.append('dbStart', dbStart);
            if (dbEnd) params.append('dbEnd', dbEnd);
            if (dauG1Start) params.append('dauG1Start', dauG1Start);
            if (dauG1End) params.append('dauG1End', dauG1End);
            if (g1Start) params.append('g1Start', g1Start);
            if (g1End) params.append('g1End', g1End);
            
            const response = await fetch(`${API_BASE_URL}?${params.toString()}`);
            
            if (!response.ok) {
                throw new Error('Không thể tải dữ liệu phân tích');
            }
            
            const result: AnalysisData = await response.json();
            setData(result);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Có lỗi xảy ra');
        } finally {
            setLoading(false);
        }
    };

    // Lưu filters vào localStorage mỗi khi chúng thay đổi
    useEffect(() => {
        const filters: FilterState = {
            fromDate,
            dauDBStart,
            dauDBEnd,
            dbStart,
            dbEnd,
            dauG1Start,
            dauG1End,
            g1Start,
            g1End
        };
        saveFiltersToStorage(filters);
    }, [fromDate, dauDBStart, dauDBEnd, dbStart, dbEnd, dauG1Start, dauG1End, g1Start, g1End]);

    useEffect(() => {
        fetchAnalysisData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [fromDate, dauDBStart, dauDBEnd, dbStart, dbEnd, dauG1Start, dauG1End, g1Start, g1End]);

    const formatDate = (dateStr: string) => {
        // dateStr format: dd-MM-yyyy
        const [day, month, year] = dateStr.split('-');
        return `${day}-${month}`;
    };

    return (
        <div className="analysis-container">
            <h2 className="analysis-title">Phân Tích Kết Quả</h2>
            
            {/* Filters */}
            <div className="analysis-filters">
                <div className="filter-group">
                    <label>Từ ngày:</label>
                    <input
                        type="date"
                        value={fromDate}
                        onChange={(e) => setFromDate(e.target.value)}
                        className="filter-input"
                    />
                </div>
                
                <div className="filter-group">
                    <label>Đầu ĐB:</label>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Bắt đầu"
                        value={dauDBStart}
                        onChange={(e) => setDauDBStart(e.target.value)}
                        className="filter-input-small"
                    />
                    <span>-</span>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Kết thúc"
                        value={dauDBEnd}
                        onChange={(e) => setDauDBEnd(e.target.value)}
                        className="filter-input-small"
                    />
                </div>
                
                <div className="filter-group">
                    <label>ĐB:</label>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Bắt đầu"
                        value={dbStart}
                        onChange={(e) => setDbStart(e.target.value)}
                        className="filter-input-small"
                    />
                    <span>-</span>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Kết thúc"
                        value={dbEnd}
                        onChange={(e) => setDbEnd(e.target.value)}
                        className="filter-input-small"
                    />
                </div>
                
                <div className="filter-group">
                    <label>Đầu G1:</label>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Bắt đầu"
                        value={dauG1Start}
                        onChange={(e) => setDauG1Start(e.target.value)}
                        className="filter-input-small"
                    />
                    <span>-</span>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Kết thúc"
                        value={dauG1End}
                        onChange={(e) => setDauG1End(e.target.value)}
                        className="filter-input-small"
                    />
                </div>
                
                <div className="filter-group">
                    <label>G1:</label>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Bắt đầu"
                        value={g1Start}
                        onChange={(e) => setG1Start(e.target.value)}
                        className="filter-input-small"
                    />
                    <span>-</span>
                    <input
                        type="number"
                        min="0"
                        max="99"
                        placeholder="Kết thúc"
                        value={g1End}
                        onChange={(e) => setG1End(e.target.value)}
                        className="filter-input-small"
                    />
                </div>
            </div>

            {/* Table */}
            {loading && <div className="loading">Đang tải dữ liệu...</div>}
            {error && <div className="error">{error}</div>}
            
            {!loading && !error && data && (
                <div className="analysis-table-wrapper">
                    <table className="analysis-table">
                        <thead>
                            <tr>
                                <th>Ngày</th>
                                <th colSpan={2}>Đầu ĐB</th>
                                <th colSpan={2}>ĐB</th>
                                <th colSpan={2}>Đầu G1</th>
                                <th colSpan={2}>G1</th>
                            </tr>
                            <tr>
                                <th></th>
                                <th>Số</th>
                                <th>KQ</th>
                                <th>Số</th>
                                <th>KQ</th>
                                <th>Số</th>
                                <th>KQ</th>
                                <th>Số</th>
                                <th>KQ</th>
                            </tr>
                        </thead>
                        <tbody>
                            {data.rows.map((row, index) => (
                                <tr key={index}>
                                    <td className="date-cell">{formatDate(row.date)}</td>
                                    <td className="number-cell">{row.dauDB !== null ? String(row.dauDB).padStart(2, '0') : '-'}</td>
                                    <td className={`match-cell ${row.dauDBMatch === 1 ? 'match' : 'no-match'}`}>
                                        {row.dauDBMatch}
                                    </td>
                                    <td className="number-cell">{row.db !== null ? String(row.db).padStart(2, '0') : '-'}</td>
                                    <td className={`match-cell ${row.dbMatch === 1 ? 'match' : 'no-match'}`}>
                                        {row.dbMatch}
                                    </td>
                                    <td className="number-cell">{row.dauG1 !== null ? String(row.dauG1).padStart(2, '0') : '-'}</td>
                                    <td className={`match-cell ${row.dauG1Match === 1 ? 'match' : 'no-match'}`}>
                                        {row.dauG1Match}
                                    </td>
                                    <td className="number-cell">{row.g1 !== null ? String(row.g1).padStart(2, '0') : '-'}</td>
                                    <td className={`match-cell ${row.g1Match === 1 ? 'match' : 'no-match'}`}>
                                        {row.g1Match}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default Analysis;

