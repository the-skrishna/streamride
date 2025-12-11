import React from 'react';

interface DurationStatsProps {
  averageDuration: number;
}

export const DurationStats: React.FC<DurationStatsProps> = ({ averageDuration }) => {
  return (
    <div className="metrics-card">
      <div className="metrics-card-header">
        <span className="metrics-icon">⏱️</span>
        <h3>Average Duration</h3>
      </div>
      <div className="metrics-card-value">{averageDuration.toFixed(2)}</div>
      <div className="metrics-card-subtitle">minutes</div>
    </div>
  );
};