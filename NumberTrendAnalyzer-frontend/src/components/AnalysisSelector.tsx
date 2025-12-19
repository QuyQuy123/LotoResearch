// src/components/AnalysisSelector.tsx
import React from 'react';
import './AnalysisSelector.css';

interface AnalysisSelectorProps {
    onSelectAlgorithm: (algorithm: string) => void;
}

const AnalysisSelector: React.FC<AnalysisSelectorProps> = ({ onSelectAlgorithm }) => {
    const algorithms = [
        {
            id: '50-50',
            name: 'Hàm 50/50',
            description: 'Phân tích kết quả với hàm 50/50',
            available: true
        },
        {
            id: 'even-odd',
            name: 'Hàm chẵn lẻ',
            description: 'Phân tích theo chẵn lẻ',
            available: true
        },
        {
            id: 'divide-3',
            name: 'Hàm chia 3',
            description: 'Phân tích theo hàm chia 3',
            available: false
        },
        {
            id: 'prime',
            name: 'Hàm số nguyên tố',
            description: 'Phân tích theo số nguyên tố',
            available: false
        },
        {
            id: 'merge-2',
            name: 'Hàm gộp 2',
            description: 'Phân tích với hàm gộp 2',
            available: false
        },
        {
            id: 'merge-3',
            name: 'Hàm gộp 3',
            description: 'Phân tích với hàm gộp 3',
            available: false
        }
    ];

    const handleSelect = (algorithm: typeof algorithms[0]) => {
        if (algorithm.available || algorithm.id === 'even-odd') {
            onSelectAlgorithm(algorithm.id);
        } else {
            alert('Chức năng đang phát triển');
        }
    };

    return (
        <div className="analysis-selector-container">
            <h2 className="selector-title">Chọn Thuật Toán Phân Tích</h2>
            <p className="selector-subtitle">Vui lòng chọn một thuật toán để bắt đầu phân tích</p>
            
            <div className="algorithms-grid">
                {algorithms.map((algorithm) => (
                    <div
                        key={algorithm.id}
                        className={`algorithm-card ${algorithm.available ? 'available' : 'unavailable'}`}
                        onClick={() => handleSelect(algorithm)}
                    >
                        <div className="algorithm-icon">
                            {algorithm.available ? '✓' : '🔒'}
                        </div>
                        <h3 className="algorithm-name">{algorithm.name}</h3>
                        <p className="algorithm-description">{algorithm.description}</p>
                        {!algorithm.available && (
                            <span className="coming-soon-badge">Đang phát triển</span>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default AnalysisSelector;

