// src/components/LotteryTable.tsx
import React from 'react';
import './LotteryTable.css'; // File CSS giữ nguyên, không cần sửa

// 1. Định nghĩa kiểu dữ liệu (Interface) cho đối tượng kết quả
// Việc này giúp code nhắc lệnh thông minh hơn
export interface LotteryData {
    date: string;
    dayOfWeek: string;
    codes?: string[]; // Dấu ? nghĩa là có thể có hoặc không (Miền Nam ko có mã)
    specialPrize: string;
    prize1: string;
    prize2: string[];
    prize3: string[];
    prize4: string[];
    prize5: string[];
    prize6: string[];
    prize7: string[];
}

// 2. Định nghĩa kiểu cho Props đầu vào của Component
interface LotteryTableProps {
    data: LotteryData;
}

const LotteryTable: React.FC<LotteryTableProps> = ({ data }) => {
    if (!data) return <p>Đang tải dữ liệu...</p>;

    return (
        <div className="lottery-shell">
            <div className="lottery-container">
                <div className="lottery-header">
                    <a href="#">XSMB</a> » <a href="#">XSMB Thứ {data.dayOfWeek}</a> » <a href="#">XSMB {data.date}</a>
                </div>

                <table className="lottery-table">
                    <tbody>
                    {/* Mã Đặc Biệt */}
                    {data.codes && (
                        <tr>
                            <td className="label-col">Mã ĐB</td>
                            <td className="content-col">
                                <div className="code-list">
                                    {data.codes.map((code, index) => (
                                        <span key={index} className="code-item">{code}</span>
                                    ))}
                                </div>
                            </td>
                        </tr>
                    )}

                    {/* Giải Đặc Biệt */}
                    <tr>
                        <td className="label-col">G.ĐB</td>
                        <td className="content-col">
                            <span className="special-prize">{data.specialPrize}</span>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.1</td>
                        <td className="content-col">
                            <span className="text-bold">{data.prize1}</span>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.2</td>
                        <td className="content-col">
                            <div className="number-row">
                                {data.prize2.map((num, i) => <span key={i} className="text-bold">{num}</span>)}
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.3</td>
                        <td className="content-col">
                            <div className="number-grid-3">
                                {data.prize3.map((num, i) => <span key={i} className="text-bold">{num}</span>)}
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.4</td>
                        <td className="content-col">
                            <div className="number-row-4">
                                {data.prize4.map((num, i) => <span key={i} className="text-bold">{num}</span>)}
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.5</td>
                        <td className="content-col">
                            <div className="number-grid-3">
                                {data.prize5.map((num, i) => <span key={i} className="text-bold">{num}</span>)}
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.6</td>
                        <td className="content-col">
                            <div className="number-row">
                                {data.prize6.map((num, i) => <span key={i} className="text-bold">{num}</span>)}
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td className="label-col">G.7</td>
                        <td className="content-col">
                            <div className="number-row-4 text-red">
                                {data.prize7.map((num, i) => <span key={i} className="text-bold">{num}</span>)}
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default LotteryTable;