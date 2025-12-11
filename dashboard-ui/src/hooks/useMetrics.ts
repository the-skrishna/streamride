import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import type { DashboardMetrics } from '../types/metrics';

export const useMetrics = () => {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchMetrics = useCallback(async () => {
        try {
            setError(null);
            const response = await axios.get<DashboardMetrics>(
                `/metrics/current`
            );
            setMetrics(response.data);
        } catch (err) {
            console.error('Failed to fetch metrics:', err);
            setError('Failed to fetch metrics');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchMetrics();
        // Polling fallback every 5 seconds if WebSocket fails
        const interval = setInterval(fetchMetrics, 5000);

        return () => clearInterval(interval);
    }, [fetchMetrics]);

    return { metrics, loading, error, refetch: fetchMetrics };
};