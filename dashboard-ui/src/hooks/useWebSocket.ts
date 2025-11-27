import { useEffect, useRef, useState, useCallback } from 'react';
import io, { Socket } from 'socket.io-client';
import { DashboardMetrics } from '../types/metrics';

export const useWebSocket = (url: string = 'http://localhost:8083') => {
    const socketRef = useRef<Socket | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        try {
            socketRef.current = io(url, {
                transports: ['websocket', 'polling'],
                reconnection: true,
                reconnectionDelay: 1000,
                reconnectionDelayMax: 5000,
                reconnectionAttempts: 5,
            });

            socketRef.current.on('connect', () => {
                console.log('WebSocket connected');
                setIsConnected(true);
                setError(null);
            });

            socketRef.current.on('disconnect', () => {
                console.log('WebSocket disconnected');
                setIsConnected(false);
            });

            socketRef.current.on('error', (err) => {
                console.error('WebSocket error:', err);
                setError('Connection error');
            });

            return () => {
                if (socketRef.current) {
                    socketRef.current.disconnect();
                }
            };
        } catch (err) {
            console.error('Failed to connect WebSocket:', err);
            setError('Failed to establish WebSocket connection');
        }
    }, [url]);

    const subscribe = useCallback(
        (event: string, callback: (data: any) => void) => {
            if (socketRef.current) {
                socketRef.current.on(event, callback);
            }
        },
        []
    );

    const unsubscribe = useCallback(
        (event: string, callback?: (data: any) => void) => {
            if (socketRef.current) {
                if (callback) {
                    socketRef.current.off(event, callback);
                } else {
                    socketRef.current.off(event);
                }
            }
        },
        []
    );

    return {
        isConnected,
        error,
        subscribe,
        unsubscribe,
        socket: socketRef.current,
    };
};