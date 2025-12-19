// src/components/PrimeAnalysis.tsx
import React, { useState, useEffect } from 'react';
import './Analysis.css';

const API_BASE_URL = 'http://localhost:8080/api/analysis';

const STORAGE_KEY = 'primeAnalysisFilters';

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

interface EmptyCount {
    emptyLength: number;
    count: number;
}

interface EmptyStats {
    columnName: string;
    range: string;
    counts: EmptyCount[];
}

interface AnalysisData {
    rows: AnalysisRow[];
    emptyStats: EmptyStats[];
    totalPages: number;
    currentPage: number;
    totalElements: number;
}

interface FilterState {
    fromDate: string;
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
        fromDate: date.toISOString().split('T')[0]
    };
};

interface PrimeAnalysisProps {
    onBack?: () => void;
}

const PrimeAnalysis: React.FC<PrimeAnalysisProps> = ({ onBack }) => {
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
    
    // Phân trang
    const [currentPage, setCurrentPage] = useState<number>(0);
    const pageSize = 30;

    // Reset về trang đầu khi thay đổi filter
    useEffect(() => {
        setCurrentPage(0);
    }, [fromDate]);

    const fetchAnalysisData = async () => {
        setLoading(true);
        setError(null);
        
        try {
            const params = new URLSearchParams();
            if (fromDate) params.append('fromDate', fromDate);
            params.append('page', currentPage.toString());
            params.append('size', pageSize.toString());
            params.append('analysisType', 'prime');
            
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
            fromDate
        };
        saveFiltersToStorage(filters);
    }, [fromDate]);

    useEffect(() => {
        fetchAnalysisData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [fromDate, currentPage]);

    const formatDate = (dateStr: string) => {
        // dateStr format: dd-MM-yyyy
        const [day, month] = dateStr.split('-');
        return `${day}-${month}`;
    };

    return (
        <div className="analysis-container">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h2 className="analysis-title">Phân Tích Kết Quả - Hàm Số Nguyên Tố</h2>
                {onBack && (
                    <button
                        onClick={onBack}
                        className="back-button"
                        style={{
                            padding: '8px 16px',
                            background: 'rgba(148, 163, 184, 0.2)',
                            border: '1px solid rgba(148, 163, 184, 0.3)',
                            borderRadius: '6px',
                            color: '#cbd5e1',
                            cursor: 'pointer',
                            fontSize: '14px',
                            fontWeight: '500',
                            transition: 'all 0.2s'
                        }}
                        onMouseOver={(e) => {
                            e.currentTarget.style.background = 'rgba(148, 163, 184, 0.3)';
                        }}
                        onMouseOut={(e) => {
                            e.currentTarget.style.background = 'rgba(148, 163, 184, 0.2)';
                        }}
                    >
                        ← Quay lại
                    </button>
                )}
            </div>
            
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
                    
                    {/* Phân trang */}
                    {data.totalPages > 1 && (
                        <div className="pagination">
                            <button
                                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                                disabled={currentPage === 0}
                                className="pagination-btn"
                            >
                                Trước
                            </button>
                            <span className="pagination-info">
                                Trang {data.currentPage + 1} / {data.totalPages} 
                                ({data.totalElements} kết quả)
                            </span>
                            <button
                                onClick={() => setCurrentPage(prev => Math.min(data.totalPages - 1, prev + 1))}
                                disabled={currentPage >= data.totalPages - 1}
                                className="pagination-btn"
                            >
                                Sau
                            </button>
                        </div>
                    )}
                </div>
            )}
            
            {/* Bảng thống kê rỗng */}
            {!loading && !error && data && data.emptyStats && data.emptyStats.length > 0 && (
                <div className="empty-stats-container">
                    <h3 className="empty-stats-title">Thống Kê Rỗng</h3>
                    <div className="empty-stats-table-wrapper">
                        <table className="empty-stats-table">
                            <thead>
                                <tr>
                                    <th>Đầu Giải</th>
                                    <th>Khoảng Lọc</th>
                                    <th>Rỗng 3</th>
                                    <th>Rỗng 4</th>
                                    <th>Rỗng 5</th>
                                    <th>Rỗng 6</th>
                                    <th>Rỗng 7</th>
                                    <th>Rỗng 8</th>
                                    <th>Rỗng 9</th>
                                    <th>Rỗng 10+</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data.emptyStats.map((stat, index) => {
                                    // Tạo map để dễ dàng truy cập
                                    const countMap = new Map<number, number>();
                                    stat.counts.forEach(c => {
                                        countMap.set(c.emptyLength, c.count);
                                    });
                                    
                                    return (
                                        <tr key={index}>
                                            <td className="stats-column-name">{stat.columnName}</td>
                                            <td className="stats-range">{stat.range}</td>
                                            <td className="stats-count">{countMap.get(3) || 0}</td>
                                            <td className="stats-count">{countMap.get(4) || 0}</td>
                                            <td className="stats-count">{countMap.get(5) || 0}</td>
                                            <td className="stats-count">{countMap.get(6) || 0}</td>
                                            <td className="stats-count">{countMap.get(7) || 0}</td>
                                            <td className="stats-count">{countMap.get(8) || 0}</td>
                                            <td className="stats-count">{countMap.get(9) || 0}</td>
                                            <td className="stats-count">
                                                {Array.from(countMap.entries())
                                                    .filter(([length]) => length >= 10)
                                                    .reduce((sum, [, count]) => sum + count, 0)}
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PrimeAnalysis;

